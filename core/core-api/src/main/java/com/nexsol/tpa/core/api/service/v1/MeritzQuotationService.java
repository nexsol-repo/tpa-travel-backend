package com.nexsol.tpa.core.api.service.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeRequest;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeResponse;
import com.nexsol.tpa.core.api.dto.v1.QuoteRequest;
import com.nexsol.tpa.core.api.dto.v1.QuoteResponse;
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
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeritzQuotationService {

    private static final String PLAN_INQ = "/b2b/v1/organ/meritz/planInq"; // 플랜조회
    private static final String HNDY_PREM_CMPT = "/b2b/v1/organ/meritz/hndyPremCmpt"; // 간편보험료 산출

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

            Map<String, QuoteResponse.Coverage> api003CoverageMap = buildApiCoverageMapKeepingUnits(prem, request, repIdx);

            List<TravelPlanCoverageRow> dbCoverages = travelPlanCoverageRepository.findRowsByPlanId(plan.getId());
            List<QuoteResponse.Coverage> merged = new ArrayList<>();

            for (TravelPlanCoverageRow row : dbCoverages) {
                if (!row.isIncluded()) continue;

                String covCd = row.getCoverageCode();

                // API001에 없는 담보는 제외
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
                            .categoryCode(row.getCategoryCode())
                            .units(List.of())
                            .build());
                } else {
                    merged.add(QuoteResponse.Coverage.builder()
                            .covCd(covCd)
                            .covNm(covNm)
                            .cur(apiCov.getCur())
                            .insdAmt(apiCov.getInsdAmt())
                            .categoryCode(row.getCategoryCode())
                            .units(apiCov.getUnits())
                            .build());
                }
            }

            List<QuoteResponse.InsuredPremium> insuredPremiums = buildInsuredPremiums(prem, request);
            String coverageTitle = buildCoverageTitle(dbCoverages, api003CoverageMap);

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
                    .insuredPremiums(insuredPremiums)
                    .coverageTitle(coverageTitle)
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

    public QuoteResponse.PlanCard quotePlanCoverages(Long planId, QuoteRequest request) {
        QuoteResponse res = quote(request);

        if (res == null || !res.isOk()) {
            // quote()가 이미 실패 포맷을 내려주므로 여기서는 예외로 처리 (ControllerAdvice로 400 변환 가능)
            throw new IllegalStateException(
                    "quote failed. errCd=" + (res == null ? "null" : res.getErrCd()) +
                            ", errMsg=" + (res == null ? "null" : res.getErrMsg())
            );
        }

        if (res.getPlans() == null || res.getPlans().isEmpty()) {
            throw new NoSuchElementException("quote result has no plans");
        }

        return res.getPlans().stream()
                .filter(p -> Objects.equals(p.getPlanId(), planId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("plan not found. planId=" + planId));
    }

    private List<QuoteResponse.InsuredPremium> buildInsuredPremiums(
            MeritzHndyPremInner prem,
            QuoteRequest request
    ) {
        List<MeritzHndyPremInner.InspeInfo> insured = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insured == null || insured.isEmpty()) return List.of();

        // 요청 insuredList와 인덱스 매칭(메리츠 응답도 동일 순서라고 가정 / 일반적으로 그럼)
        int n = Math.min(insured.size(),
                request.getInsuredList() == null ? 0 : request.getInsuredList().size());

        List<QuoteResponse.InsuredPremium> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            MeritzHndyPremInner.InspeInfo m = insured.get(i);
            QuoteRequest.Insured r = request.getInsuredList().get(i);

            // ppsPrem은 "4110" 처럼 문자열 -> long
            long ppsPrem = parseLong(m.getPpsPrem());

            out.add(QuoteResponse.InsuredPremium.builder()
                    .index(i)
                    .currency("KRW")
                    .ppsPrem(ppsPrem)
                    .birth(r.getBirth())
                    .gndrCd(m.getGndrCd())
                    .cusNm(m.getCusNm())
                    .cusEngNm(m.getCusEngNm())
                    // .ageBandCode(band.code())
                    // .ageBandLabel(band.label())
                    .build());
        }
        return List.copyOf(out);
    }

    private enum AgeBand {
        AGE_0_14("AGE_0_14", "0~14세", 0, 14),
        AGE_15_69("AGE_15_69", "15~69세", 15, 69);

        private final String code;
        private final String label;
        private final int min;
        private final int max;

        AgeBand(String code, String label, int min, int max) {
            this.code = code;
            this.label = label;
            this.min = min;
            this.max = max;
        }

        static AgeBand fromAge(int age) {
            for (AgeBand b : values()) {
                if (age >= b.min && age <= b.max) return b;
            }
            return null; // 가입불가(예: 70+) 처리용
        }
    }

    private int calcAgeAtStartDate(String birthYmd, String insBgnDtYmd) {
        // birth: "YYYYMMDD" / insBgnDt: "YYYYMMDD"
        LocalDate birth = LocalDate.parse(birthYmd, YYYYMMDD);
        LocalDate std = LocalDate.parse(insBgnDtYmd, YYYYMMDD);
        return java.time.Period.between(birth, std).getYears(); // 만 나이(보험개시일 기준)
    }

    private String buildCoverageTitle(List<TravelPlanCoverageRow> dbCoverages,
                                      Map<String, QuoteResponse.Coverage> api003CoverageMap) {

        // titleYn = 1 인 담보들만, sortOrder 순서대로 "담보명 금액" 형태로 만들기
        List<String> parts = new ArrayList<>();

        for (TravelPlanCoverageRow row : dbCoverages) {
            if (!row.isIncluded()) continue;
            if (!row.isTitleYn()) continue;

            String covCd = row.getCoverageCode();

            String name = (row.getDisplayName() != null && !row.getDisplayName().isBlank())
                    ? row.getDisplayName()
                    : row.getCoverageName();

            QuoteResponse.Coverage apiCov = api003CoverageMap.get(covCd);
            long amt = (apiCov != null) ? apiCov.getInsdAmt() : 0L;

            // 0이면 굳이 타이틀에 넣지 않거나, 넣고 싶으면 정책대로
            if (amt <= 0) continue;

            parts.add(name + " " + formatWonShort(amt));
        }

        if (parts.isEmpty()) return null;
        return "보장금액 : " + String.join(" / ", parts);
    }

    private String formatWonShort(long amount) {
        // 아주 단순화 버전: 억/만원 단위
        long eok = amount / 100_000_000L;
        long man = (amount % 100_000_000L) / 10_000L;

        if (eok > 0 && man == 0) return eok + "억원";
        if (eok > 0) return eok + "억" + String.format("%,d", man) + "만원";
        if (man > 0) return String.format("%,d", man) + "만원";
        return String.format("%,d", amount) + "원";
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

    private Map<String, QuoteResponse.Coverage> buildApiCoverageMapKeepingUnits(
            MeritzHndyPremInner prem,
            QuoteRequest request,
            int repIdx
    ) {
        Map<String, List<QuoteResponse.CoverageUnit>> unitsByCovCd = extractUnitsByCovCdGroupedByAgeBand(prem, request);

        Map<String, Long> repInsdAmtByCovCd = extractRepInsdAmtByCovCd(prem, repIdx);
        Map<String, String> curByCovCd = extractCurrencyByCovCd(prem, repIdx);

        Map<String, QuoteResponse.Coverage> map = new HashMap<>();
        for (String covCd : unitsByCovCd.keySet()) {
            List<QuoteResponse.CoverageUnit> units = unitsByCovCd.getOrDefault(covCd, List.of());
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

    private Map<String, List<Object>> extractUnitsByCovCdGroupedByAgeBandAsObject(
            MeritzHndyPremInner prem,
            QuoteRequest req
    ) {
        // covCd -> ageBandCode -> unitMap
        Map<String, Map<String, Map<String, Object>>> acc = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty()) return Map.of();

        for (int i = 0; i < insuredList.size(); i++) {
            var insured = insuredList.get(i);
            if (req.getInsuredList() == null || req.getInsuredList().size() <= i) continue;

            var reqInsured = req.getInsuredList().get(i);

            String birthYmd = reqInsured.getBirth();   // 네 QuoteRequest 구조에 맞게
            int age = calcAgeAtStartDate(birthYmd, req.getInsBgnDt());
            AgeBand band = AgeBand.fromAge(age);
            if (band == null) continue;

            var covs = insured.getOpapiGnrCoprCtrQuotCovInfCbcVo();
            if (covs == null) continue;

            for (var c : covs) {
                String covCd = c.getCovCd();
                if (covCd == null || covCd.isBlank()) continue;

                long insdAmt = parseLong(c.getInsdAmt());
                long premAmt = parseLong(c.getPrem());

                acc.computeIfAbsent(covCd, k -> new LinkedHashMap<>());
                Map<String, Map<String, Object>> byBand = acc.get(covCd);

                Map<String, Object> unit = byBand.get(band.code);
                if (unit == null) {
                    unit = new LinkedHashMap<>();
                    unit.put("ageBandCode", band.code);
                    unit.put("ageBandLabel", band.label);
                    unit.put("count", 0);
                    unit.put("insdAmt", insdAmt);
                    unit.put("premSum", 0L);
                    byBand.put(band.code, unit);
                }

                unit.put("count", ((int) unit.get("count")) + 1);
                unit.put("premSum", ((long) unit.get("premSum")) + premAmt);

                // insdAmt 정책(최대값)
                long current = (long) unit.get("insdAmt");
                if (insdAmt > current) unit.put("insdAmt", insdAmt);
            }
        }

        Map<String, List<Object>> out = new HashMap<>();
        for (var e : acc.entrySet()) {
            out.put(e.getKey(), List.copyOf(e.getValue().values()));
        }
        return out;
    }

    private Map<String, List<QuoteResponse.CoverageUnit>> extractUnitsByCovCdGroupedByAgeBand(
            MeritzHndyPremInner prem,
            QuoteRequest req
    ) {
        Map<String, Map<String, QuoteResponse.CoverageUnit>> acc = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty()) return Map.of();

        // insured index 기준으로 request insuredList와 매칭 (너 코드 전제 그대로)
        for (int i = 0; i < insuredList.size(); i++) {
            var insured = insuredList.get(i);

            if (req.getInsuredList() == null || req.getInsuredList().size() <= i) continue;
            var reqInsured = req.getInsuredList().get(i);

            // birth는 너 QuoteRequest 구조에 맞게 가져와
            String birth = reqInsured.getBirth(); // "YYYYMMDD"
            int age = calcAgeAtStartDate(birth, req.getInsBgnDt());
            AgeBand band = AgeBand.fromAge(age);

            // 가입불가 밴드는 스킵 (또는 별도 처리)
            if (band == null) continue;

            List<MeritzHndyPremInner.CovInfo> covs = insured.getOpapiGnrCoprCtrQuotCovInfCbcVo();
            if (covs == null) continue;

            for (var c : covs) {
                String covCd = c.getCovCd();
                if (covCd == null || covCd.isBlank()) continue;

                long insdAmt = parseLong(c.getInsdAmt());
                long premAmt = parseLong(c.getPrem()); // prem이 "1699.325" 처럼 소수면 parseLong(BigDecimal)로 처리됨

                acc.computeIfAbsent(covCd, k -> new LinkedHashMap<>());
                Map<String, QuoteResponse.CoverageUnit> byBand = acc.get(covCd);

                QuoteResponse.CoverageUnit unit = byBand.get(band.code);
                if (unit == null) {
                    unit = QuoteResponse.CoverageUnit.builder()
                            .ageBandCode(band.code)
                            .ageBandLabel(band.label)
                            .count(0)
                            .insdAmt(insdAmt)
                            .premSum(0L)
                            .build();
                    byBand.put(band.code, unit);
                }

                // 같은 연령대 인원 추가
                unit.setCount(unit.getCount() + 1);

                // 연령대 보험료 합 (원하면)
                unit.setPremSum(unit.getPremSum() + premAmt);

                // 보장금액은 같은 연령대면 일반적으로 동일하지만,
                // 혹시라도 다르면 "최대값" 같은 정책으로 통일 가능
                if (insdAmt > unit.getInsdAmt()) {
                    unit.setInsdAmt(insdAmt);
                }
            }
        }

        // 최종: covCd -> (연령대별 unit 리스트)
        Map<String, List<QuoteResponse.CoverageUnit>> out = new HashMap<>();
        for (var e : acc.entrySet()) {
            out.put(e.getKey(), List.copyOf(e.getValue().values()));
        }
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

    private Map<String, MeritzHndyPremInner> callApi003Parallel(
            String companyCode,
            QuoteRequest req,
            List<TravelInsurancePlanEntity> plans
    ) {
        // =========================
        // Step 0) 동시 호출 제한 (플랜 3개)
        // - fixed thread pool size = min(3, planCount)
        // =========================
        int poolSize = Math.min(3, Math.max(1, plans.size()));
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        try {
            // =========================
            // Step 1) 플랜별 비동기 호출 작업 생성
            // =========================
            List<CompletableFuture<Map.Entry<String, MeritzHndyPremInner>>> futures = plans.stream()
                    .map(plan -> CompletableFuture.supplyAsync(() -> {
                        String planCd = plan.getPlanCode();
                        try {
                            // Step 1-1) API003 호출
                            MeritzHndyPremInner prem = callHndyPremCmpt(companyCode, req, plan);

                            // Step 1-2) 응답 errCd 확인 (00001 성공)
                            if (prem == null) {
                                log.warn("[MERITZ][HNDY_PREM_CMPT][FAIL] planCd={}, prem=null", planCd);
                                return null;
                            }
                            if (!"00001".equals(prem.getErrCd())) {
                                log.warn("[MERITZ][HNDY_PREM_CMPT][FAIL] planCd={}, errCd={}, errMsg={}",
                                        planCd, prem.getErrCd(), prem.getErrMsg());
                                return null;
                            }

                            // Step 1-3) 성공 결과 반환 (planCd -> prem)
                            return Map.entry(planCd, prem);

                        } catch (Exception e) {
                            // Step 1-4) 예외는 해당 플랜만 실패 처리
                            log.warn("[MERITZ][HNDY_PREM_CMPT][EXCEPTION] planCd={}, msg={}", planCd, e.getMessage(), e);
                            return null;
                        }
                    }, executor))
                    .toList();

            // =========================
            // Step 2) 전체 타임아웃 설정 (예: 8초)
            // - 플랜이 3개라 충분히 짧게 잡아도 됨
            // =========================
            CompletableFuture<Void> all =
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {
                all.get(30, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                log.warn("[MERITZ][HNDY_PREM_CMPT][TIMEOUT] timeout=8s, plans={}",
                        plans.stream().map(TravelInsurancePlanEntity::getPlanCode).toList());
                // 타임아웃이 나도 가능한 것만 취합 (부분 성공 허용)
            } catch (InterruptedException ie) {
                // 인터럽트는 반드시 현재 스레드에 다시 표시해주는 게 정석
                Thread.currentThread().interrupt();
                log.warn("[MERITZ][HNDY_PREM_CMPT][INTERRUPTED] plans={}",
                        plans.stream().map(TravelInsurancePlanEntity::getPlanCode).toList(), ie);
                // 그래도 부분 성공 취합은 진행 가능
            } catch (ExecutionException ee) {
                // allOf는 내부 future 중 하나라도 예외면 ExecutionException이 날 수 있음
                log.warn("[MERITZ][HNDY_PREM_CMPT][EXECUTION_EXCEPTION] plans={}",
                        plans.stream().map(TravelInsurancePlanEntity::getPlanCode).toList(), ee);
                // 그래도 부분 성공 취합은 진행 가능
            }

            // =========================
            // Step 3) 결과 취합 (null 제외)
            // =========================
            Map<String, MeritzHndyPremInner> out = new LinkedHashMap<>();
            for (CompletableFuture<Map.Entry<String, MeritzHndyPremInner>> f : futures) {
                try {
                    Map.Entry<String, MeritzHndyPremInner> e = f.getNow(null);
                    if (e != null && e.getKey() != null && e.getValue() != null) {
                        out.put(e.getKey(), e.getValue());
                    }
                } catch (Exception ignore) {
                    // getNow는 예외 거의 안 나지만 안전하게 무시
                }
            }

            return out;

        } finally {
            // =========================
            // Step 4) Executor 종료
            // =========================
            executor.shutdownNow();
        }
    }

    private record PlanKey(String untPdCd, String pdCd, String planGrpCd, String planCd) {
        static PlanKey from(TravelInsurancePlanEntity p) {
            return new PlanKey(
                    p.getUnitProductCode(),
                    p.getProductCode(),
                    p.getPlanGroupCode(),
                    p.getPlanCode()
            );
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MeritzPlanInqInner {
        private String errCd;
        private String errMsg;
        private String inqCnt;
        private List<PlanCovRow> opapiGnrPdPlanInfCbcVo;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class PlanCovRow {
            private String stdDt;
            private String pdCd;
            private String untPdCd;
            private String planGrpCd;
            private String planGrpNm;
            private String planCd;
            private String planNm;

            private String covCd;
            private String covNm;
            private String sbcAmtCurCd;
            private String sbcAmt;
            private String owbrAmt;
        }
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
