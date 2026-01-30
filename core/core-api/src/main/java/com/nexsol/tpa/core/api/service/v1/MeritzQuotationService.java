package com.nexsol.tpa.core.api.service.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeRequest;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeResponse;
import com.nexsol.tpa.core.api.dto.QuoteRequest;
import com.nexsol.tpa.core.api.dto.QuoteResponse;
import com.nexsol.tpa.core.api.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.core.api.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.core.api.repository.v1.TravelInsurancePlanRepository;
import com.nexsol.tpa.core.api.repository.v1.TravelPlanCoverageRepository;
import com.nexsol.tpa.core.api.repository.v1.projection.TravelPlanCoverageRow;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeritzQuotationService {

    private static final String HNDY_PREM_CMPT = "/b2b/v1/organ/meritz/hndyPremCmpt";

    // 고정값
    private static final String FIXED_GNR_AFLCO_CD = "020";
    private static final String FIXED_AFLCO_DIV_CD = "TPA";
    private static final String FIXED_BIZPE_NO = "2368801872";
    private static final String FIXED_POL_NO = "15540-148539";

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MeritzBridgeClient bridgeClient;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    private final TravelInsurancePlanRepository travelInsurancePlanRepository;
    private final TravelPlanCoverageRepository travelPlanCoverageRepository;

    public QuoteResponse quote(QuoteRequest request) {

        // =========================
        // Step 1) 입력검증
        // =========================
        int repIdx = request.getRepresentativeIndex() == null ? 0 : request.getRepresentativeIndex();
        if (request.getInsuredList() == null || request.getInsuredList().isEmpty()) {
            return QuoteResponse.fail("400", "insuredList is empty", null);
        }
        if (repIdx < 0 || repIdx >= request.getInsuredList().size()) {
            return QuoteResponse.fail("400", "representativeIndex is invalid", null);
        }
        Long insurerId = request.getInsurerId();
        if (insurerId == null) return QuoteResponse.fail("400", "insurerId is required", null);

        // =========================
        // Step 2) DB 플랜 조회 (정렬/노출 기준)
        // =========================
        List<TravelInsurancePlanEntity> dbPlans = travelInsurancePlanRepository.findActiveByInsurerId(insurerId);
        if (dbPlans.isEmpty()) return QuoteResponse.fail("NO_PLAN", "DB에 활성 플랜이 없습니다.", null);

        // =========================
        // Step 3) API001 호출 (플랜조회: stdDt는 planInq 요청 값)
        //  - stdDt는 우선 request.insBgnDt 써도 되고, 지금은 니가 준 예시처럼 고정일 수도 있음
        //  - 추천: request.insBgnDt 를 stdDt로 사용 (yyyyMMdd 포맷 동일)
        // =========================
        String stdDt = request.getInsBgnDt(); // ✅ 일단 이걸로 확정 추천
        MeritzPlanInqInner inq = callPlanInq("TPA", stdDt);

        // Step 3-1) API001 결과를 PlanKey(4키)로 그룹핑
        Map<PlanKey, Map<String, MeritzPlanInqInner.PlanCovRow>> api001ByPlanKey = groupPlanInq(inq);

        // =========================
        // Step 4) DB 플랜과 API001 플랜 매핑(4키) → 대상 플랜 확정
        // =========================
        List<TravelInsurancePlanEntity> targetPlans = new ArrayList<>();
        for (TravelInsurancePlanEntity p : dbPlans) {
            PlanKey k = PlanKey.from(p);
            if (api001ByPlanKey.containsKey(k)) {
                targetPlans.add(p);
            }
        }
        if (targetPlans.isEmpty()) {
            return QuoteResponse.fail("NO_PLAN", "API001 기준 제공되는 플랜이 없습니다.", null);
        }

        // =========================
        // Step 5) API003(간편보험료산출) - 플랜별 호출 (병렬 추천: 플랜 3개)
        // =========================
        // 여기서 premByPlanCd = Map<planCd, MeritzHndyPremInner> 로 받으면,
        // API003 응답이 planCd만 있어도 매칭 가능.
        Map<String, MeritzHndyPremInner> premByPlanCd = callApi003Parallel("TPA", request, targetPlans);

        // =========================
        // Step 6) 응답 조립
        //  - travel_plan_coverage 기준으로 담보 구성
        //  - 단, API001에서 제공되는 covCd만 포함 (핵심!)
        // =========================
        List<QuoteResponse.PlanCard> cards = new ArrayList<>();

        for (TravelInsurancePlanEntity plan : targetPlans) {
            MeritzHndyPremInner prem = premByPlanCd.get(plan.getPlanCode());
            if (prem == null || !"00001".equals(prem.getErrCd())) continue;

            // API001 covCd map (이 플랜이 제공하는 담보 목록)
            Map<String, MeritzPlanInqInner.PlanCovRow> api001CovMap = api001ByPlanKey.get(PlanKey.from(plan));

            Map<String, QuoteResponse.Coverage> api003CoverageMap = buildApiCoverageMapKeepingUnits(prem, repIdx);

            List<TravelPlanCoverageRow> dbCoverages = travelPlanCoverageRepository.findRowsByPlanId(plan.getId());
            List<QuoteResponse.Coverage> merged = new ArrayList<>();

            for (TravelPlanCoverageRow row : dbCoverages) {
                if (!row.isIncluded()) continue;

                String covCd = row.getCoverageCode();

                // ✅ API001에 없는 담보는 제외
                if (api001CovMap == null || !api001CovMap.containsKey(covCd)) continue;

                String covNm = (row.getDisplayName() != null && !row.getDisplayName().isBlank())
                        ? row.getDisplayName()
                        : row.getCoverageName();

                QuoteResponse.Coverage apiCov = api003CoverageMap.get(covCd);
                if (apiCov == null) {
                    merged.add(QuoteResponse.Coverage.builder()
                            .covCd(covCd)
                            .covNm(covNm)
                            .cur("KRW")
                            .insdAmt(0L)
                            .units(List.of())
                            .build());
                } else {
                    merged.add(QuoteResponse.Coverage.builder()
                            .covCd(covCd)
                            .covNm(covNm)
                            .cur(apiCov.getCur())
                            .insdAmt(apiCov.getInsdAmt())
                            .units(apiCov.getUnits())
                            .build());
                }
            }

            cards.add(QuoteResponse.PlanCard.builder()
                    .planId(plan.getId())
                    .planGrpCd(plan.getPlanGroupCode())
                    .planCd(plan.getPlanCode())
                    .planNm(plan.getPlanName())
                    .planNmRaw(plan.getPlanFullName() == null ? plan.getPlanName() : plan.getPlanFullName())
                    .premium(QuoteResponse.Premium.builder()
                            .ttPrem(parseLong(prem.getTtPrem()))
                            .currency("KRW")
                            .build())
                    .coverages(merged)
                    .build());
        }

        if (cards.isEmpty()) {
            return QuoteResponse.fail("PREM_FAIL", "보험료 산출 결과가 없습니다.", null);
        }

        return QuoteResponse.success(
                QuoteResponse.Period.builder()
                        .insBgnDt(request.getInsBgnDt())
                        .insEdDt(request.getInsEdDt())
                        .build(),
                repIdx,
                request.getInsuredList().size(),
                cards
        );
    }

    private MeritzPlanInqInner callPlanInq(String companyCode, String stdDt) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve(companyCode);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", FIXED_GNR_AFLCO_CD);
        body.put("aflcoDivCd", FIXED_AFLCO_DIV_CD);
        body.put("bizpeNo", FIXED_BIZPE_NO);
        body.put("polNo", FIXED_POL_NO);   // ✅ 상수 확정
        body.put("stdDt", stdDt);

        logJson("[MERITZ][PLAN_INQ][REQ]", "stdDt=" + stdDt, body);

        MeritzBridgeResponse res = bridgeClient.call(
                new MeritzBridgeRequest(
                        cfg.getCompanyCode(),
                        PLAN_INQ,
                        "POST",
                        headers(),
                        body
                )
        );

        if (res.getStatus() != 200) {
            throw new IllegalStateException("Meritz planInq failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        MeritzPlanInqInner inner = readInner(res.getBody(), MeritzPlanInqInner.class);
        if (!"00001".equals(inner.getErrCd())) {
            throw new IllegalStateException("Meritz planInq errCd=" + inner.getErrCd() + ", errMsg=" + inner.getErrMsg());
        }
        return inner;
    }

    private Map<PlanKey, Map<String, MeritzPlanInqInner.PlanCovRow>> groupPlanInq(MeritzPlanInqInner inq) {
        Map<PlanKey, Map<String, MeritzPlanInqInner.PlanCovRow>> out = new HashMap<>();

        if (inq.getOpapiGnrPdPlanInfCbcVo() == null) return out;

        for (MeritzPlanInqInner.PlanCovRow r : inq.getOpapiGnrPdPlanInfCbcVo()) {
            PlanKey key = new PlanKey(r.getUntPdCd(), r.getPdCd(), r.getPlanGrpCd(), r.getPlanCd());
            out.computeIfAbsent(key, k -> new LinkedHashMap<>()) // covCd map (순서 유지)
                    .put(r.getCovCd(), r);
        }
        return out;
    }

    private Map<String, QuoteResponse.Coverage> buildApiCoverageMapKeepingUnits(MeritzHndyPremInner prem, int repIdx) {
        Map<String, List<Object>> unitsByCovCd = extractUnitsByCovCd(prem);
        Map<String, Long> repInsdAmtByCovCd = extractRepInsdAmtByCovCd(prem, repIdx);
        Map<String, String> curByCovCd = extractCurrencyByCovCd(prem, repIdx);

        Map<String, QuoteResponse.Coverage> map = new HashMap<>();
        for (String covCd : unitsByCovCd.keySet()) {
            List<Object> units = unitsByCovCd.getOrDefault(covCd, List.of());
            long insdAmt = repInsdAmtByCovCd.getOrDefault(covCd, 0L);
            String cur = curByCovCd.getOrDefault(covCd, "KRW");

            map.put(covCd, QuoteResponse.Coverage.builder()
                    .covCd(covCd)
                    .covNm(null)
                    .cur(cur)
                    .insdAmt(insdAmt)
                    .units(units)
                    .build());
        }
        return map;
    }

    private Map<String, List<Object>> extractUnitsByCovCd(MeritzHndyPremInner prem) {
        Map<String, List<Object>> out = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty()) return out;

        for (MeritzHndyPremInner.InspeInfo insured : insuredList) {
            List<MeritzHndyPremInner.CovInfo> covs = insured.getOpapiGnrCoprCtrQuotCovInfCbcVo();
            if (covs == null) continue;

            for (MeritzHndyPremInner.CovInfo c : covs) {
                String covCd = c.getCovCd();
                if (covCd == null || covCd.isBlank()) continue;

                Map<String, Object> unit = new LinkedHashMap<>();
                unit.put("covCd", c.getCovCd());
                unit.put("covNm", c.getCovNm());
                unit.put("insdAmt", c.getInsdAmt());
                unit.put("prem", c.getPrem());
                unit.put("sbcAmtCurCd", c.getSbcAmtCurCd());

                out.computeIfAbsent(covCd, k -> new ArrayList<>()).add(unit);
            }
        }

        out.replaceAll((k, v) -> List.copyOf(v));
        return out;
    }

    private Map<String, Long> extractRepInsdAmtByCovCd(MeritzHndyPremInner prem, int repIdx) {
        Map<String, Long> out = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty()) return out;

        int idx = Math.max(0, Math.min(repIdx, insuredList.size() - 1));
        MeritzHndyPremInner.InspeInfo rep = insuredList.get(idx);

        List<MeritzHndyPremInner.CovInfo> repCovs = rep.getOpapiGnrCoprCtrQuotCovInfCbcVo();
        if (repCovs == null) return out;

        for (MeritzHndyPremInner.CovInfo c : repCovs) {
            String covCd = c.getCovCd();
            if (covCd == null || covCd.isBlank()) continue;
            out.put(covCd, parseLong(c.getInsdAmt()));
        }
        return out;
    }

    private Map<String, String> extractCurrencyByCovCd(MeritzHndyPremInner prem, int repIdx) {
        Map<String, String> out = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty()) return out;

        int idx = Math.max(0, Math.min(repIdx, insuredList.size() - 1));
        MeritzHndyPremInner.InspeInfo rep = insuredList.get(idx);

        List<MeritzHndyPremInner.CovInfo> repCovs = rep.getOpapiGnrCoprCtrQuotCovInfCbcVo();
        if (repCovs == null) return out;

        for (MeritzHndyPremInner.CovInfo c : repCovs) {
            String covCd = c.getCovCd();
            if (covCd == null || covCd.isBlank()) continue;

            String cur = (c.getSbcAmtCurCd() == null || c.getSbcAmtCurCd().isBlank())
                    ? "KRW"
                    : c.getSbcAmtCurCd();

            out.put(covCd, cur);
        }
        return out;
    }

    // =========================
    // bridge 연동 (핵심: body를 Object로 전달)
    // =========================

    private MeritzHndyPremInner callHndyPremCmpt(String companyCode, QuoteRequest req, TravelInsurancePlanEntity plan) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve(companyCode);

        String sbcpDt = LocalDate.now().format(YYYYMMDD);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", FIXED_GNR_AFLCO_CD);
        body.put("aflcoDivCd", FIXED_AFLCO_DIV_CD);
        body.put("bizpeNo", FIXED_BIZPE_NO);
        body.put("polNo", FIXED_POL_NO);

        body.put("pdCd", plan.getProductCode());
        body.put("untPdCd", plan.getUnitProductCode());

        body.put("sbcpDt", sbcpDt);
        body.put("insBgnDt", req.getInsBgnDt());
        body.put("insEdDt", req.getInsEdDt());
        body.put("trvArCd", req.getTrvArCd());
        body.put("inspeCnt", req.getInsuredList().size());

        List<Map<String, Object>> insuredVos = req.getInsuredList().stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("planGrpCd", plan.getPlanGroupCode());
            m.put("planCd", plan.getPlanCode());
            m.put("inspeBdt", i.getBirth());
            m.put("gndrCd", i.getGender());

            // TODO: 실데이터로 교체
            m.put("inspeNm", "홍길동");
            m.put("engInspeNm", "HONG GIL DONG");
            return m;
        }).toList();
        body.put("opapiTrvPremCmptInspeInfCbcVo", insuredVos);

        logJson("[MERITZ][HNDY_PREM_CMPT][REQ]", plan.getPlanCode(), body);
        MeritzBridgeResponse res = bridgeClient.call(
                new MeritzBridgeRequest(
                        cfg.getCompanyCode(),
                        HNDY_PREM_CMPT,
                        "POST",
                        headers(),
                        body
                )
        );

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz hndyPremCmpt failed. meritzStatus=" + res.getStatus() + ", body=" + res.getBody());
        }

        return readInner(res.getBody(), MeritzHndyPremInner.class);
    }

    private Map<String, String> headers() {
        return Map.of("Content-Type", "application/json; charset=UTF-8");
    }

    private void logJson(String prefix, String planCd, Object body) {
        try {
            log.info("{} planCd={}, body={}", prefix, planCd, objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.info("{} planCd={}, body=(json serialize fail) {}", prefix, planCd, body);
        }
    }

    private <T> T readInner(String innerJson, Class<T> type) {
        try {
            return objectMapper.readValue(innerJson, type);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse meritz response inner JSON. type=" + type.getSimpleName(), e);
        }
    }

    private CompaniesConfigsProperties.CompanyConfig resolve(String companyCode) {
        if (companyCode == null || companyCode.isBlank()) companyCode = "TPA";
        if ("TPA".equalsIgnoreCase(companyCode)) return companies.getTpa();
        if ("INSBOON".equalsIgnoreCase(companyCode)) return companies.getInsboon();
        throw new IllegalArgumentException("Unknown companyCode: " + companyCode);
    }

    // =========================
    // util
    // =========================

    private long parseLong(String s) {
        try {
            if (s == null) return 0L;
            return new BigDecimal(s.trim()).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    private String mapUserFriendlyErrMsg(String errCd, String raw) {
        if ("69999".equals(errCd)) {
            return "어린이는 가입이 불가합니다. 가입 조건을 확인 바랍니다.";
        }
        return (raw == null || raw.isBlank()) ? "처리 중 오류가 발생했습니다." : raw;
    }

    private static class PlanQuoteResult {
        private final TravelInsurancePlanEntity plan;
        private final MeritzHndyPremInner prem;

        private PlanQuoteResult(TravelInsurancePlanEntity plan, MeritzHndyPremInner prem) {
            this.plan = plan;
            this.prem = prem;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MeritzHndyPremInner {
        private String errCd;
        private String errMsg;
        private String ttPrem;
        private String polNo;

        private List<InspeInfo> opapiGnrCoprCtrInspeInfCbcVo;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class InspeInfo {
            private String cusNm;
            private String cusEngNm;
            private String gndrCd;
            private String planCd;
            private String ppsPrem;
            private List<CovInfo> opapiGnrCoprCtrQuotCovInfCbcVo;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class CovInfo {
            private String covCd;
            private String covNm;
            private String sbcAmtCurCd;
            private String insdAmt;
            private String prem;
        }
    }
}
