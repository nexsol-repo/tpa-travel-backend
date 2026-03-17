package com.nexsol.tpa.core.domain.plan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;

import lombok.RequiredArgsConstructor;

/**
 * 플랜 조회 비즈니스 서비스 (Business Layer).
 * PlanReader(조회) + QuotePlanPolicy(규칙)를 조합한다.
 */
@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanReader planReader;
    private final QuotePlanPolicy policy;

    /**
     * 일반 견적 대상 패밀리를 조회한다.
     * validate → 전체 패밀리 로딩 → planType 결정 → 일반 패밀리만 필터.
     */
    public List<PlanFamily> findQuoteFamilies(PlanCondition cmd) {
        validateQuoteCommand(cmd);

        List<PlanFamily> allFamilies = planReader.loadAllFamilies(cmd.insurerId());
        String planType = policy.resolvePlanType(cmd.insuredList(), cmd.insBgnDt());
        boolean silsonExclude = cmd.silsonExclude() != null && cmd.silsonExclude();
        List<PlanFamily> families = policy.filterFamilies(allFamilies, planType, silsonExclude);

        if (families.isEmpty()) {
            throw new CoreApiException(
                    CoreApiErrorType.QUOTE_PLAN_NOT_FOUND, "플랜타입(" + planType + ") 기준 플랜 없음");
        }
        return families;
    }

    /**
     * planId가 속한 패밀리 1개를 조회한다.
     * 실손포함/제외 구분 없이 전체 패밀리에서 검색한다.
     */
    public PlanFamily findFamilyByPlanId(PlanCondition cmd, Long planId) {
        validateQuoteCommand(cmd);

        List<PlanFamily> allFamilies = planReader.loadAllFamilies(cmd.insurerId());
        String planType = policy.resolvePlanType(cmd.insuredList(), cmd.insBgnDt());
        String typeMarker = "플랜" + planType;

        return allFamilies.stream()
                .filter(f -> f.familyName() != null && f.familyName().contains(typeMarker))
                .filter(f -> f.plans().stream().anyMatch(p -> p.getId().equals(planId)))
                .findFirst()
                .orElseThrow(
                        () ->
                                new CoreApiException(
                                        CoreApiErrorType.QUOTE_PLAN_NOT_FOUND, "planId=" + planId));
    }

    /**
     * 실손포함 대표 planId → 대응하는 실손제외 대표 planId 매핑을 반환한다.
     * planCode 기반 매칭: "TA21" (실손포함) → "TA21P" (실손제외)
     */
    public Map<Long, Long> findSilsonExcludePlanIdMap(PlanCondition cmd) {
        validateQuoteCommand(cmd);

        List<PlanFamily> allFamilies = planReader.loadAllFamilies(cmd.insurerId());
        String planType = policy.resolvePlanType(cmd.insuredList(), cmd.insBgnDt());

        List<PlanFamily> lossFamilies = policy.filterFamilies(allFamilies, planType, false);
        List<PlanFamily> excludeFamilies = policy.filterFamilies(allFamilies, planType, true);

        // 실손제외 planCode → repPlanId 인덱싱
        Map<String, Long> excludeByCode = new HashMap<>();
        for (PlanFamily f : excludeFamilies) {
            excludeByCode.put(f.repPlan().getPlanCode(), f.repPlan().getId());
        }

        // 실손포함 repPlanId → 실손제외 repPlanId 매핑 (planCode + "P")
        Map<Long, Long> result = new HashMap<>();
        for (PlanFamily f : lossFamilies) {
            String excludeCode = f.repPlan().getPlanCode() + "P";
            Long excludePlanId = excludeByCode.get(excludeCode);
            if (excludePlanId != null) {
                result.put(f.repPlan().getId(), excludePlanId);
            }
        }
        return result;
    }

    // ── 내부 로직 ──

    private void validateQuoteCommand(PlanCondition cmd) {
        if (cmd.insuredList() == null || cmd.insuredList().isEmpty()) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_QUOTE_REQUEST, "insuredList is empty");
        }
        if (cmd.insurerId() == null) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_QUOTE_REQUEST, "insurerId is required");
        }
        int repIdx = cmd.representativeIndex() == null ? 0 : cmd.representativeIndex();
        if (repIdx < 0 || repIdx >= cmd.insuredList().size()) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_QUOTE_REQUEST, "representativeIndex is invalid");
        }
    }
}
