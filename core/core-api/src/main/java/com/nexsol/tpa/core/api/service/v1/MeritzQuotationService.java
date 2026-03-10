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
import com.nexsol.tpa.core.api.repository.v1.projection.PlanFamilyPlanRow;
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

    private static final String HNDY_PREM_CMPT = "/b2b/v1/organ/meritz/hndyPremCmpt"; // 간편보험료
                                                                                      // 산출

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
        if (insurerId == null)
            return QuoteResponse.fail("400", "insurerId is required", null);

        // =========================
        // Step 2) DB 플랜 조회 (정렬/노출 기준) + 카드(패밀리) 그룹핑
        // =========================

        // 2-1) 패밀리+플랜 조인 결과(이미 정렬된 상태로 가져오기)
        List<PlanFamilyPlanRow> rows = travelInsurancePlanRepository.findActiveFamilyPlans(insurerId);
        if (rows.isEmpty()) {
            return QuoteResponse.fail("NO_PLAN", "DB에 활성 플랜이 없습니다.", null);
        }

        // 2-2) planId 목록 수집 → plan 엔티티 한 번에 조회
        Set<Long> planIds = new LinkedHashSet<>();
        for (PlanFamilyPlanRow r : rows) {
            planIds.add(r.getPlanId());
        }

        List<TravelInsurancePlanEntity> plans = travelInsurancePlanRepository.findAllById(planIds);
        if (plans.isEmpty()) {
            return QuoteResponse.fail("NO_PLAN", "패밀리 매핑 plan_id에 해당하는 플랜이 없습니다.", null);
        }

        Map<Long, TravelInsurancePlanEntity> planById = new HashMap<>();
        for (TravelInsurancePlanEntity p : plans) {
            planById.put(p.getId(), p);
        }

        // 2-3) familyId(Long) 기준으로 카드 그룹핑 (여기가 정답)
        Map<Long, List<TravelInsurancePlanEntity>> families = new LinkedHashMap<>();
        Map<Long, String> familyNameById = new LinkedHashMap<>();
        for (PlanFamilyPlanRow r : rows) {
            TravelInsurancePlanEntity p = planById.get(r.getPlanId());
            if (p == null)
                continue;

            families.computeIfAbsent(r.getFamilyId(), k -> new ArrayList<>()).add(p);

            familyNameById.putIfAbsent(r.getFamilyId(), r.getFamilyName());
        }

        if (families.isEmpty()) {
            return QuoteResponse.fail("NO_PLAN", "DB에 카드(패밀리) 플랜이 없습니다.", null);
        }

        // 2-4) 카드별 대표플랜 선정 (ageGroupId=2 우선)
        Map<Long, TravelInsurancePlanEntity> familyRepPlan = new LinkedHashMap<>();
        for (var e : families.entrySet()) {
            List<TravelInsurancePlanEntity> list = e.getValue();
            if (list == null || list.isEmpty())
                continue;

            TravelInsurancePlanEntity rep = list.stream()
                .filter(p -> Objects.equals(p.getAgeGroupId(), 2))
                .findFirst()
                .orElse(list.get(0));

            familyRepPlan.put(e.getKey(), rep);
        }

        // =========================
        // Step 3) API001 호출 (플랜조회: stdDt는 planInq 요청 값)
        // - stdDt는 우선 request.insBgnDt 써도 되고, 지금은 니가 준 예시처럼 고정일 수도 있음
        // - 추천: request.insBgnDt 를 stdDt로 사용 (yyyyMMdd 포맷 동일)
        // =========================
        String stdDt = request.getInsBgnDt(); // 일단 이걸로 확정 추천
        MeritzPlanInqInner inq = callPlanInq("TPA", stdDt);

        // Step 3-1) API001 결과를 PlanKey(4키)로 그룹핑
        Map<PlanKey, Map<String, MeritzPlanInqInner.PlanCovRow>> api001ByPlanKey = groupPlanInq(inq);

        // =========================
        // Step 4) 카드(패밀리) 중 API001에 존재하는 것만 필터링
        // - key는 familyId(Long)
        // =========================
        Map<Long, List<TravelInsurancePlanEntity>> targetFamilies = new LinkedHashMap<>();

        for (var e : families.entrySet()) {
            Long familyId = e.getKey();
            TravelInsurancePlanEntity repPlan = familyRepPlan.get(familyId);

            if (repPlan == null)
                continue;

            PlanKey k = PlanKey.from(repPlan);
            if (api001ByPlanKey.containsKey(k)) {
                targetFamilies.put(familyId, e.getValue());
            }
        }

        if (targetFamilies.isEmpty()) {
            return QuoteResponse.fail("NO_PLAN", "API001 기준 제공되는 플랜(카드)이 없습니다.", null);
        }

        // =========================
        // Step 5) API003(간편보험료산출) - 플랜별 호출 (병렬 추천: 플랜 3개)
        // =========================
        // 여기서 premByPlanCd = Map<planCd, MeritzHndyPremInner> 로 받으면,
        // API003 응답이 planCd만 있어도 매칭 가능.
        Map<String, MeritzHndyPremInner> premByPlanCd = callApi003Serial("TPA", request, targetFamilies);

        // =========================
        // Step 6) 응답 조립 (패밀리 카드 기준 3개)
        // - travel_plan_coverage 기준으로 담보 구성
        // - 단, API001에서 제공되는 covCd만 포함 (핵심!)
        // =========================
        List<QuoteResponse.PlanCard> cards = new ArrayList<>();

        for (var entry : targetFamilies.entrySet().stream().limit(15).toList()) {

            Long familyId = entry.getKey();
            List<TravelInsurancePlanEntity> familyPlans = entry.getValue();
            if (familyPlans == null || familyPlans.isEmpty())
                continue;

            // 대표 플랜은 Step2에서 선정한 것을 그대로 사용 (재계산 금지)
            TravelInsurancePlanEntity repPlan = familyRepPlan.get(familyId);
            if (repPlan == null)
                continue;

            // Step5에서 premByPlanCd의 key를 "대표 planCd"로 넣었으니까 그대로 조회
            MeritzHndyPremInner prem = premByPlanCd.get(repPlan.getPlanCode());
            if (prem == null || !"00001".equals(prem.getErrCd())) {
                continue;
            }

            // API001 covCd map: 패밀리 내 모든 planCd의 covCd를 합침 (ageGroup별 담보가 다를 수 있음)
            Map<String, MeritzPlanInqInner.PlanCovRow> api001CovMap = new LinkedHashMap<>();
            for (TravelInsurancePlanEntity fp : familyPlans) {
                Map<String, MeritzPlanInqInner.PlanCovRow> perPlan = api001ByPlanKey.get(PlanKey.from(fp));
                if (perPlan != null) {
                    perPlan.forEach(api001CovMap::putIfAbsent);
                }
            }

            Map<String, QuoteResponse.Coverage> api003CoverageMap = buildApiCoverageMapKeepingUnits(prem, request,
                    repIdx);

            // 담보는 대표 플랜의 plan_id 기준 (패밀리 공통 담보셋)
            List<TravelPlanCoverageRow> dbCoverages = travelPlanCoverageRepository.findRowsByPlanId(repPlan.getId());

            log.info("[COVERAGE][DEBUG] familyId={}, dbCovCds={}, api001CovCds={}",
                    familyId,
                    dbCoverages.stream().filter(TravelPlanCoverageRow::isIncluded)
                        .map(r -> r.getCoverageCode() + "(" + r.getCategoryCode() + ")").toList(),
                    api001CovMap.keySet());

            List<QuoteResponse.Coverage> merged = new ArrayList<>();

            for (TravelPlanCoverageRow row : dbCoverages) {
                if (!row.isIncluded())
                    continue;

                String covCd = row.getCoverageCode();

                // API001에 없는 담보는 제외
                if (api001CovMap == null || !api001CovMap.containsKey(covCd)) {
                    log.info("[COVERAGE][SKIP] covCd={}, category={}, not in API001", covCd, row.getCategoryCode());
                    continue;
                }

                String covNm = (row.getDisplayName() != null && !row.getDisplayName().isBlank()) ? row.getDisplayName()
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
                }
                else {
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
            String familyName = familyNameById.get(familyId);

            cards.add(QuoteResponse.PlanCard.builder()
                .planId(repPlan.getId())
                .planGrpCd(repPlan.getPlanGroupCode())
                .planCd(repPlan.getPlanCode())
                .planNm(familyName)
                .planNmRaw(repPlan.getPlanFullName() == null ? repPlan.getPlanName() : repPlan.getPlanFullName())
                .premium(QuoteResponse.Premium.builder().ttPrem(parseLong(prem.getTtPrem())).currency("KRW").build())
                .insuredPremiums(insuredPremiums)
                .coverageTitle(coverageTitle)
                .coverages(merged)
                .build());
        }

        if (cards.isEmpty()) {
            return QuoteResponse.fail("PREM_FAIL", "보험료 산출 결과가 없습니다.", null);
        }

        return QuoteResponse.success(
                QuoteResponse.Period.builder().insBgnDt(request.getInsBgnDt()).insEdDt(request.getInsEdDt()).build(),
                repIdx, request.getInsuredList().size(), cards);

    }

    // 나이 체크
    private Integer mapAgeToAgeGroupId(int age) {
        if (age >= 0 && age <= 14)
            return 1;
        if (age >= 15 && age <= 69)
            return 2;
        if (age >= 70 && age <= 80)
            return 3;
        return null; // 81+ 또는 음수 등은 미지원(메리츠 거절 케이스)
    }

    // 플랜 카드
    private record PlanFamilyKey(String pdCd, String untPdCd, String planGrpCd) {
        static PlanFamilyKey from(TravelInsurancePlanEntity p) {
            return new PlanFamilyKey(p.getProductCode(), p.getUnitProductCode(), p.getPlanGroupCode());
        }
    }

    public QuoteResponse.PlanCard quotePlanCoverages(Long planId, QuoteRequest request) {
        QuoteResponse res = quote(request);

        if (res == null || !res.isOk()) {
            // quote()가 이미 실패 포맷을 내려주므로 여기서는 예외로 처리 (ControllerAdvice로 400 변환 가능)
            throw new IllegalStateException("quote failed. errCd=" + (res == null ? "null" : res.getErrCd())
                    + ", errMsg=" + (res == null ? "null" : res.getErrMsg()));
        }

        if (res.getPlans() == null || res.getPlans().isEmpty()) {
            throw new NoSuchElementException("quote result has no plans");
        }

        return res.getPlans()
            .stream()
            .filter(p -> Objects.equals(p.getPlanId(), planId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("plan not found. planId=" + planId));
    }

    private List<QuoteResponse.InsuredPremium> buildInsuredPremiums(MeritzHndyPremInner prem, QuoteRequest request) {
        List<MeritzHndyPremInner.InspeInfo> insured = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insured == null || insured.isEmpty())
            return List.of();

        // 요청 insuredList와 인덱스 매칭(메리츠 응답도 동일 순서라고 가정 / 일반적으로 그럼)
        int n = Math.min(insured.size(), request.getInsuredList() == null ? 0 : request.getInsuredList().size());

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

        AGE_0_14("AGE_0_14", "0~14세", 0, 14), AGE_15_69("AGE_15_69", "15~69세", 15, 69),
        AGE_70_80("AGE_70_80", "70~80세", 70, 80);

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
                if (age >= b.min && age <= b.max)
                    return b;
            }
            return null;
        }

        static AgeBand fromCode(String code) {
            for (AgeBand b : values()) {
                if (b.code.equals(code))
                    return b;
            }
            return null;
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
            if (!row.isIncluded())
                continue;
            if (!row.isTitleYn())
                continue;

            String covCd = row.getCoverageCode();

            String name = (row.getDisplayName() != null && !row.getDisplayName().isBlank()) ? row.getDisplayName()
                    : row.getCoverageName();

            QuoteResponse.Coverage apiCov = api003CoverageMap.get(covCd);
            long amt = (apiCov != null) ? apiCov.getInsdAmt() : 0L;

            // 0이면 굳이 타이틀에 넣지 않거나, 넣고 싶으면 정책대로
            if (amt <= 0)
                continue;

            parts.add(name + " " + formatWonShort(amt));
        }

        if (parts.isEmpty())
            return null;
        return "보장금액 : " + String.join(" / ", parts);
    }

    private String formatWonShort(long amount) {
        // 아주 단순화 버전: 억/만원 단위
        long eok = amount / 100_000_000L;
        long man = (amount % 100_000_000L) / 10_000L;

        if (eok > 0 && man == 0)
            return eok + "억원";
        if (eok > 0)
            return eok + "억" + String.format("%,d", man) + "만원";
        if (man > 0)
            return String.format("%,d", man) + "만원";
        return String.format("%,d", amount) + "원";
    }

    private MeritzPlanInqInner callPlanInq(String companyCode, String stdDt) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve(companyCode);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", cfg.getPolNo());
        body.put("stdDt", stdDt);

        logJson("[MERITZ][PLAN_INQ][REQ]", "stdDt=" + stdDt, body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), PLAN_INQ, "POST", headers(), body));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz planInq failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        MeritzPlanInqInner inner = readInner(res.getBody(), MeritzPlanInqInner.class);
        if (!"00001".equals(inner.getErrCd())) {
            throw new IllegalStateException(
                    "Meritz planInq errCd=" + inner.getErrCd() + ", errMsg=" + inner.getErrMsg());
        }
        return inner;
    }

    private Map<PlanKey, Map<String, MeritzPlanInqInner.PlanCovRow>> groupPlanInq(MeritzPlanInqInner inq) {
        Map<PlanKey, Map<String, MeritzPlanInqInner.PlanCovRow>> out = new HashMap<>();

        if (inq.getOpapiGnrPdPlanInfCbcVo() == null)
            return out;

        for (MeritzPlanInqInner.PlanCovRow r : inq.getOpapiGnrPdPlanInfCbcVo()) {
            PlanKey key = new PlanKey(r.getUntPdCd(), r.getPdCd(), r.getPlanGrpCd(), r.getPlanCd());
            out.computeIfAbsent(key, k -> new LinkedHashMap<>()) // covCd map (순서 유지)
                .put(r.getCovCd(), r);
        }
        return out;
    }

    private Map<String, QuoteResponse.Coverage> buildApiCoverageMapKeepingUnits(MeritzHndyPremInner prem,
            QuoteRequest request, int repIdx) {
        Map<String, List<QuoteResponse.CoverageUnit>> unitsByCovCd = extractUnitsByCovCdGroupedByAgeBand(prem, request);

        Map<String, Long> repInsdAmtByCovCd = extractRepInsdAmtByCovCd(prem, repIdx);
        Map<String, String> curByCovCd = extractCurrencyByCovCd(prem, repIdx);

        Map<String, QuoteResponse.Coverage> map = new HashMap<>();
        for (String covCd : unitsByCovCd.keySet()) {
            List<QuoteResponse.CoverageUnit> units = unitsByCovCd.getOrDefault(covCd, List.of());
            long insdAmt = repInsdAmtByCovCd.getOrDefault(covCd, 0L);
            String cur = curByCovCd.getOrDefault(covCd, "KRW");

            map.put(covCd,
                    QuoteResponse.Coverage.builder()
                        .covCd(covCd)
                        .covNm(null)
                        .cur(cur)
                        .insdAmt(insdAmt)
                        .units(units)
                        .build());
        }
        return map;
    }

    private Map<String, List<Object>> extractUnitsByCovCdGroupedByAgeBandAsObject(MeritzHndyPremInner prem,
            QuoteRequest req) {
        // covCd -> ageBandCode -> unitMap
        Map<String, Map<String, Map<String, Object>>> acc = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty())
            return Map.of();

        for (int i = 0; i < insuredList.size(); i++) {
            var insured = insuredList.get(i);
            if (req.getInsuredList() == null || req.getInsuredList().size() <= i)
                continue;

            var reqInsured = req.getInsuredList().get(i);

            String birthYmd = reqInsured.getBirth(); // 네 QuoteRequest 구조에 맞게
            int age = calcAgeAtStartDate(birthYmd, req.getInsBgnDt());
            AgeBand band = AgeBand.fromAge(age);
            if (band == null)
                continue;

            var covs = insured.getOpapiGnrCoprCtrQuotCovInfCbcVo();
            if (covs == null)
                continue;

            for (var c : covs) {
                String covCd = c.getCovCd();
                if (covCd == null || covCd.isBlank())
                    continue;

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
                if (insdAmt > current)
                    unit.put("insdAmt", insdAmt);
            }
        }

        Map<String, List<Object>> out = new HashMap<>();
        for (var e : acc.entrySet()) {
            out.put(e.getKey(), List.copyOf(e.getValue().values()));
        }
        return out;
    }

    private Map<String, List<QuoteResponse.CoverageUnit>> extractUnitsByCovCdGroupedByAgeBand(MeritzHndyPremInner prem,
            QuoteRequest req) {
        Map<String, Map<String, QuoteResponse.CoverageUnit>> acc = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty())
            return Map.of();

        // 요청에 포함된 모든 연령대 수집
        Set<AgeBand> requestedBands = new LinkedHashSet<>();
        for (QuoteRequest.Insured ri : req.getInsuredList()) {
            int age = calcAgeAtStartDate(ri.getBirth(), req.getInsBgnDt());
            AgeBand band = AgeBand.fromAge(age);
            if (band != null)
                requestedBands.add(band);
        }

        for (int i = 0; i < insuredList.size(); i++) {
            var insured = insuredList.get(i);

            if (req.getInsuredList() == null || req.getInsuredList().size() <= i)
                continue;
            var reqInsured = req.getInsuredList().get(i);

            String birth = reqInsured.getBirth();
            int age = calcAgeAtStartDate(birth, req.getInsBgnDt());
            AgeBand band = AgeBand.fromAge(age);

            if (band == null)
                continue;

            List<MeritzHndyPremInner.CovInfo> covs = insured.getOpapiGnrCoprCtrQuotCovInfCbcVo();
            if (covs == null)
                continue;

            for (var c : covs) {
                String covCd = c.getCovCd();
                if (covCd == null || covCd.isBlank())
                    continue;

                long insdAmt = parseLong(c.getInsdAmt());
                long premAmt = parseLong(c.getPrem());

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

                unit.setCount(unit.getCount() + 1);
                unit.setPremSum(unit.getPremSum() + premAmt);

                if (insdAmt > unit.getInsdAmt()) {
                    unit.setInsdAmt(insdAmt);
                }
            }
        }

        // 요청에 있는 연령대인데 해당 담보에 데이터가 없으면
        // ageBandCode/ageBandLabel만 채우고 나머지 null (프론트: "보장없음")
        for (var e : acc.entrySet()) {
            Map<String, QuoteResponse.CoverageUnit> byBand = e.getValue();
            for (AgeBand band : requestedBands) {
                if (!byBand.containsKey(band.code)) {
                    byBand.put(band.code,
                            QuoteResponse.CoverageUnit.builder()
                                .ageBandCode(band.code)
                                .ageBandLabel(band.label)
                                .count(null)
                                .insdAmt(null)
                                .premSum(null)
                                .build());
                }
            }
        }

        Map<String, List<QuoteResponse.CoverageUnit>> out = new HashMap<>();
        for (var e : acc.entrySet()) {
            List<QuoteResponse.CoverageUnit> sorted = e.getValue()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(u -> {
                    AgeBand b = AgeBand.fromCode(u.getAgeBandCode());
                    return b != null ? b.min : Integer.MAX_VALUE;
                }))
                .toList();
            out.put(e.getKey(), sorted);
        }
        return out;
    }

    private Map<String, Long> extractRepInsdAmtByCovCd(MeritzHndyPremInner prem, int repIdx) {
        Map<String, Long> out = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty())
            return out;

        int idx = Math.max(0, Math.min(repIdx, insuredList.size() - 1));
        MeritzHndyPremInner.InspeInfo rep = insuredList.get(idx);

        List<MeritzHndyPremInner.CovInfo> repCovs = rep.getOpapiGnrCoprCtrQuotCovInfCbcVo();
        if (repCovs == null)
            return out;

        for (MeritzHndyPremInner.CovInfo c : repCovs) {
            String covCd = c.getCovCd();
            if (covCd == null || covCd.isBlank())
                continue;
            out.put(covCd, parseLong(c.getInsdAmt()));
        }
        return out;
    }

    private Map<String, String> extractCurrencyByCovCd(MeritzHndyPremInner prem, int repIdx) {
        Map<String, String> out = new HashMap<>();

        List<MeritzHndyPremInner.InspeInfo> insuredList = prem.getOpapiGnrCoprCtrInspeInfCbcVo();
        if (insuredList == null || insuredList.isEmpty())
            return out;

        int idx = Math.max(0, Math.min(repIdx, insuredList.size() - 1));
        MeritzHndyPremInner.InspeInfo rep = insuredList.get(idx);

        List<MeritzHndyPremInner.CovInfo> repCovs = rep.getOpapiGnrCoprCtrQuotCovInfCbcVo();
        if (repCovs == null)
            return out;

        for (MeritzHndyPremInner.CovInfo c : repCovs) {
            String covCd = c.getCovCd();
            if (covCd == null || covCd.isBlank())
                continue;

            String cur = (c.getSbcAmtCurCd() == null || c.getSbcAmtCurCd().isBlank()) ? "KRW" : c.getSbcAmtCurCd();

            out.put(covCd, cur);
        }
        return out;
    }

    // =========================
    // 간편 보험료 산출 (패밀리 단위 1회 호출)
    // =========================
    private MeritzHndyPremInner callHndyPremCmpt(String companyCode, QuoteRequest req,
            List<TravelInsurancePlanEntity> familyPlans) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve(companyCode);

        if (familyPlans == null || familyPlans.isEmpty()) {
            throw new IllegalArgumentException("familyPlans is empty");
        }

        // ageGroupId -> plan
        Map<Integer, TravelInsurancePlanEntity> planByAgeGroup = new HashMap<>();
        for (TravelInsurancePlanEntity p : familyPlans) {
            if (p.getAgeGroupId() != null) { // 필드명 맞춰
                planByAgeGroup.put(p.getAgeGroupId(), p);
            }
        }

        // 대표 플랜: ageGroupId=2 우선, 없으면 첫 번째
        TravelInsurancePlanEntity repPlan = familyPlans.stream()
            .filter(p -> Objects.equals(p.getAgeGroupId(), 2))
            .findFirst()
            .orElse(familyPlans.get(0));

        String sbcpDt = LocalDate.now().format(YYYYMMDD);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", cfg.getPolNo());

        // family 공통 값은 대표 플랜에서
        body.put("pdCd", repPlan.getProductCode());
        body.put("untPdCd", repPlan.getUnitProductCode());

        body.put("sbcpDt", sbcpDt);
        body.put("insBgnDt", req.getInsBgnDt());
        body.put("insEdDt", req.getInsEdDt());
        body.put("trvArCd", req.getTrvArCd());
        body.put("inspeCnt", req.getInsuredList().size());

        List<Map<String, Object>> insuredVos = new ArrayList<>();

        for (int idx = 0; idx < req.getInsuredList().size(); idx++) {
            QuoteRequest.Insured i = req.getInsuredList().get(idx);

            int age = calcAgeAtStartDate(i.getBirth(), req.getInsBgnDt());
            Integer ageGroupId = mapAgeToAgeGroupId(age); // 너가 만든 함수(1/2/3)
            if (ageGroupId == null) {
                throw new IllegalArgumentException("unsupported age. index=" + idx + ", age=" + age);
            }

            TravelInsurancePlanEntity planForAge = planByAgeGroup.get(ageGroupId);
            if (planForAge == null) {
                throw new IllegalStateException("family has no plan for ageGroupId=" + ageGroupId + ", familyRepPlanCd="
                        + repPlan.getPlanCode());
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("planGrpCd", planForAge.getPlanGroupCode());
            m.put("planCd", planForAge.getPlanCode());
            m.put("inspeBdt", i.getBirth());
            m.put("gndrCd", i.getGender());

            // TODO: 실데이터로 교체
            m.put("inspeNm", "홍길동");
            m.put("engInspeNm", "HONG GIL DONG");

            insuredVos.add(m);
        }

        body.put("opapiTrvPremCmptInspeInfCbcVo", insuredVos);

        logJson("[MERITZ][HNDY_PREM_CMPT][REQ]", repPlan.getPlanCode(), body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), HNDY_PREM_CMPT, "POST", headers(), body));

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
        }
        catch (Exception e) {
            log.info("{} planCd={}, body=(json serialize fail) {}", prefix, planCd, body);
        }
    }

    private <T> T readInner(String innerJson, Class<T> type) {
        try {
            return objectMapper.readValue(innerJson, type);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to parse meritz response inner JSON. type=" + type.getSimpleName(),
                    e);
        }
    }

    private CompaniesConfigsProperties.CompanyConfig resolve(String companyCode) {
        if (companyCode == null || companyCode.isBlank())
            companyCode = "TPA";
        if ("TPA".equalsIgnoreCase(companyCode))
            return companies.getTpa();
        if ("INSBOON".equalsIgnoreCase(companyCode))
            return companies.getInsboon();
        throw new IllegalArgumentException("Unknown companyCode: " + companyCode);
    }

    // =========================
    // util
    // =========================

    private long parseLong(String s) {
        try {
            if (s == null)
                return 0L;
            return new BigDecimal(s.trim()).longValue();
        }
        catch (Exception e) {
            return 0L;
        }
    }

    private String mapUserFriendlyErrMsg(String errCd, String raw) {
        if ("69999".equals(errCd)) {
            return "어린이는 가입이 불가합니다. 가입 조건을 확인 바랍니다.";
        }
        return (raw == null || raw.isBlank()) ? "처리 중 오류가 발생했습니다." : raw;
    }

    // =========================
    // 간편 보험료 산출 - 직렬
    // =========================
    private Map<String, MeritzHndyPremInner> callApi003Serial(String companyCode, QuoteRequest req,
            Map<Long, List<TravelInsurancePlanEntity>> targetFamilies) {

        Map<String, MeritzHndyPremInner> premByPlanCd = new LinkedHashMap<>();

        for (var entry : targetFamilies.entrySet()) {

            Long familyId = entry.getKey();
            List<TravelInsurancePlanEntity> familyPlans = entry.getValue();
            if (familyPlans == null || familyPlans.isEmpty())
                continue;

            // 대표 플랜 선택 (15~69세 우선)
            TravelInsurancePlanEntity repPlan = familyPlans.stream()
                .filter(p -> Objects.equals(p.getAgeGroupId(), 2))
                .findFirst()
                .orElse(familyPlans.get(0));

            String repPlanCd = repPlan.getPlanCode();

            try {
                MeritzHndyPremInner prem = callHndyPremCmpt(companyCode, req, familyPlans);

                if (prem == null) {
                    log.warn("[MERITZ][HNDY_PREM_CMPT][NULL] familyId={}, repPlanCd={}", familyId, repPlanCd);
                    continue;
                }

                if (!"00001".equals(prem.getErrCd())) {
                    log.warn("[MERITZ][HNDY_PREM_CMPT][FAIL] familyId={}, repPlanCd={}, errCd={}, errMsg={}", familyId,
                            repPlanCd, prem.getErrCd(), prem.getErrMsg());
                    continue;
                }

                premByPlanCd.put(repPlanCd, prem);

            }
            catch (Exception e) {
                log.warn("[MERITZ][HNDY_PREM_CMPT][EXCEPTION] familyId={}, repPlanCd={}, msg={}", familyId, repPlanCd,
                        e.getMessage(), e);
            }
        }

        log.info("[API003][SERIAL_DONE] requested={}, success={}", targetFamilies.size(), premByPlanCd.size());

        return premByPlanCd;
    }

    // =========================
    // 간편 보험료 산출 - 병렬
    // =========================
    private Map<String, MeritzHndyPremInner> callApi003Parallel(String companyCode, QuoteRequest req,
            Map<Long, List<TravelInsurancePlanEntity>> targetFamilies) {
        // =========================
        // Step 0) 동시 호출 제한 (패밀리 카드 3개)
        // =========================
        int poolSize = Math.min(15, Math.max(1, targetFamilies.size()));
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        try {
            // =========================
            // Step 1) 패밀리별 비동기 호출 작업 생성
            // =========================
            List<CompletableFuture<Map.Entry<String, MeritzHndyPremInner>>> futures = targetFamilies.entrySet()
                .stream()
                .limit(15) // 카드 3개만 호출할 거면 여기서 제한
                .map(entry -> CompletableFuture.supplyAsync(() -> {

                    Long familyId = entry.getKey(); // PlanFamilyKey -> Long
                    List<TravelInsurancePlanEntity> familyPlans = entry.getValue();
                    if (familyPlans == null || familyPlans.isEmpty())
                        return null;

                    // 대표 플랜: ageGroupId=2 우선, 없으면 첫 번째
                    TravelInsurancePlanEntity repPlan = familyPlans.stream()
                        .filter(p -> Objects.equals(p.getAgeGroupId(), 2))
                        .findFirst()
                        .orElse(familyPlans.get(0));

                    String repPlanCd = repPlan.getPlanCode();

                    try {
                        // Step 1-1) API003 호출 (패밀리 단위)
                        MeritzHndyPremInner prem = callHndyPremCmpt(companyCode, req, familyPlans);

                        // Step 1-2) 응답 errCd 확인 (00001 성공)
                        if (prem == null) {
                            log.warn("[MERITZ][HNDY_PREM_CMPT][FAIL] familyId={}, repPlanCd={}, prem=null", familyId,
                                    repPlanCd);
                            return null;
                        }
                        if (!"00001".equals(prem.getErrCd())) {
                            log.warn("[MERITZ][HNDY_PREM_CMPT][FAIL] familyId={}, repPlanCd={}, errCd={}, errMsg={}",
                                    familyId, repPlanCd, prem.getErrCd(), prem.getErrMsg());
                            return null;
                        }

                        // Step 1-3) 성공 결과 반환 (대표 planCd -> prem)
                        return Map.entry(repPlanCd, prem);

                    }
                    catch (Exception e) {
                        log.warn("[MERITZ][HNDY_PREM_CMPT][EXCEPTION] familyId={}, repPlanCd={}, msg={}", familyId,
                                repPlanCd, e.getMessage(), e);
                        return null;
                    }
                }, executor))
                .toList();

            // =========================
            // Step 2) 전체 타임아웃
            // =========================
            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                all.get(30, TimeUnit.SECONDS);
            }
            catch (TimeoutException te) {
                log.warn("[MERITZ][HNDY_PREM_CMPT][TIMEOUT] timeout=30s, familyIds={}", targetFamilies.keySet());
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("[MERITZ][HNDY_PREM_CMPT][INTERRUPTED] familyIds={}", targetFamilies.keySet(), ie);
            }
            catch (ExecutionException ee) {
                log.warn("[MERITZ][HNDY_PREM_CMPT][EXECUTION_EXCEPTION] familyIds={}", targetFamilies.keySet(), ee);
            }

            // =========================
            // Step 3) 결과 취합 (null 제외)
            // =========================
            Map<String, MeritzHndyPremInner> out = new LinkedHashMap<>();
            for (CompletableFuture<Map.Entry<String, MeritzHndyPremInner>> f : futures) {
                Map.Entry<String, MeritzHndyPremInner> e = f.getNow(null);
                if (e != null && e.getKey() != null && e.getValue() != null) {
                    out.put(e.getKey(), e.getValue());
                }
            }
            return out;

        }
        finally {
            executor.shutdownNow();
        }
    }

    private record PlanKey(String untPdCd, String pdCd, String planGrpCd, String planCd) {
        static PlanKey from(TravelInsurancePlanEntity p) {
            return new PlanKey(p.getUnitProductCode(), p.getProductCode(), p.getPlanGroupCode(), p.getPlanCode());
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
