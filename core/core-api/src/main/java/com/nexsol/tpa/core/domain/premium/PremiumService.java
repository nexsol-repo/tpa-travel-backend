package com.nexsol.tpa.core.domain.premium;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.quote.MeritzQuoteClient;
import com.nexsol.tpa.client.meritz.quote.MeritzQuoteClient.PremiumRequest;
import com.nexsol.tpa.core.domain.plan.QuotePlanPolicy;
import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 보험료 산출 비즈니스 서비스 (Business Layer).
 * QuoteClient(API 호출) + Aggregator(집계)를 조합한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DEFAULT_COMPANY = "TPA";

    private final MeritzQuoteClient quoteClient;
    private final PremiumAggregator aggregator;
    private final QuotePlanPolicy policy;
    private final ObjectMapper objectMapper;

    /**
     * 패밀리 목록에 대해 보험료를 일괄 산출한다.
     * key: repPlan.id, value: 산출 결과 (실패 시 skip).
     */
    public Map<Long, PremiumResult> calculateAll(PlanCondition cmd, List<PlanFamily> families) {
        int repIdx = cmd.representativeIndex() == null ? 0 : cmd.representativeIndex();
        Map<Long, PremiumResult> results = new LinkedHashMap<>();

        for (PlanFamily family : families) {
            JsonNode rawData = callApi(cmd, family.plans());
            if (rawData == null) {
                log.warn(
                        "[PREMIUM] calculation skipped. familyId={}, planCd={}",
                        family.familyId(),
                        family.repPlan().getPlanCode());
                continue;
            }
            results.put(family.repPlan().getId(), aggregator.aggregate(rawData, cmd, repIdx));
        }
        return results;
    }

    /**
     * 단일 패밀리에 대해 보험료를 산출한다.
     * 실손제외 등 단건 산출에 사용. 실패 시 예외 발생.
     */
    public PremiumResult calculateSingle(PlanCondition cmd, PlanFamily family, int repIdx) {
        JsonNode rawData = callApi(cmd, family.plans());
        if (rawData == null) {
            throw new CoreApiException(
                    CoreApiErrorType.PREMIUM_CALCULATION_FAILED,
                    "familyId=" + family.familyId() + ", planCd=" + family.repPlan().getPlanCode());
        }
        return aggregator.aggregate(rawData, cmd, repIdx);
    }

    // ── internal ──

    /**
     * 도메인 로직(나이/플랜 매핑) 수행 후 MeritzQuoteClient를 호출한다.
     */
    private JsonNode callApi(PlanCondition cmd, List<TravelInsurancePlanEntity> familyPlans) {
        Map<Integer, TravelInsurancePlanEntity> planByAgeGroup = new HashMap<>();
        for (TravelInsurancePlanEntity p : familyPlans) {
            if (p.getAgeGroupId() != null) {
                planByAgeGroup.put(p.getAgeGroupId(), p);
            }
        }

        TravelInsurancePlanEntity repPlan =
                familyPlans.stream()
                        .filter(p -> Objects.equals(p.getAgeGroupId(), 2))
                        .findFirst()
                        .orElse(familyPlans.getFirst());

        List<PremiumRequest.InsuredPerson> insuredList =
                buildInsuredList(cmd, planByAgeGroup, repPlan);
        if (insuredList == null) {
            return null;
        }

        PremiumRequest request =
                new PremiumRequest(
                        DEFAULT_COMPANY,
                        repPlan.getProductCode(),
                        repPlan.getUnitProductCode(),
                        LocalDate.now().format(YYYYMMDD),
                        cmd.insBgnDt(),
                        cmd.insEdDt(),
                        cmd.trvArCd(),
                        insuredList);

        String rawJson = quoteClient.calculatePremium(request);
        if (rawJson == null) {
            return null;
        }
        return objectMapper.readTree(rawJson);
    }

    private List<PremiumRequest.InsuredPerson> buildInsuredList(
            PlanCondition cmd,
            Map<Integer, TravelInsurancePlanEntity> planByAgeGroup,
            TravelInsurancePlanEntity repPlan) {

        List<PremiumRequest.InsuredPerson> insuredList = new ArrayList<>();
        for (int i = 0; i < cmd.insuredList().size(); i++) {
            PlanCondition.Insured insured = cmd.insuredList().get(i);
            int age = policy.calcAge(insured.birth(), cmd.insBgnDt());
            Integer ageGroupId = policy.resolveAgeGroupId(age);
            if (ageGroupId == null) {
                throw new CoreApiException(
                        CoreApiErrorType.INVALID_QUOTE_REQUEST,
                        "unsupported age. index=" + i + ", age=" + age);
            }

            TravelInsurancePlanEntity planForAge = planByAgeGroup.get(ageGroupId);
            if (planForAge == null) {
                log.debug(
                        "[PREMIUM] family has no plan for ageGroupId={}, repPlanCd={} → skip",
                        ageGroupId,
                        repPlan.getPlanCode());
                return null;
            }

            insuredList.add(
                    new PremiumRequest.InsuredPerson(
                            planForAge.getPlanGroupCode(),
                            planForAge.getPlanCode(),
                            insured.birth(),
                            insured.gender(),
                            "홍길동",
                            "HONG GIL DONG"));
        }
        return insuredList;
    }
}
