package com.nexsol.tpa.core.domain.plan;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

/**
 * 플랜 조회 비즈니스 서비스 (Business Layer).
 * PlanReader(조회) + QuotePlanPolicy(규칙)를 조합한다.
 */
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanReader planReader;
    private final QuotePlanPolicy policy;

    /**
     * 일반 견적 대상 패밀리를 조회한다.
     * validate → 전체 패밀리 로딩 → planType 결정 → 일반 패밀리만 필터.
     */
    public List<PlanFamily> findQuoteFamilies(PlanCondition cmd) {
        validateQuoteCommand(cmd);

        List<PlanFamily> allFamilies = planReader.loadAllFamilies(cmd.insurerId());
        String planType = policy.resolvePlanType(cmd.insuredList());
        boolean silsonExclude = cmd.silsonExclude() != null && cmd.silsonExclude();
        List<PlanFamily> families = policy.filterFamilies(allFamilies, planType, silsonExclude);

        if (families.isEmpty()) {
            throw new CoreException(
                    CoreErrorType.QUOTE_PLAN_NOT_FOUND, "플랜타입(" + planType + ") 기준 플랜 없음");
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
        String planType = policy.resolvePlanType(cmd.insuredList());
        String typeMarker = "플랜" + planType;

        return allFamilies.stream()
                .filter(f -> f.familyName() != null && f.familyName().contains(typeMarker))
                .filter(f -> f.plans().stream().anyMatch(p -> p.id().equals(planId)))
                .findFirst()
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.QUOTE_PLAN_NOT_FOUND, "planId=" + planId));
    }

    /**
     * 실손제외 플랜 매핑을 반환한다.
     * silsonExclude=true이면 빈 맵, false이면 실손포함→실손제외 planId 매핑.
     */
    public Map<Long, Long> resolveSilsonExcludeMap(PlanCondition cmd) {
        boolean silsonExclude = cmd.silsonExclude() != null && cmd.silsonExclude();
        if (silsonExclude) {
            return Collections.emptyMap();
        }
        return findSilsonExcludePlanIdMap(cmd);
    }

    private Map<Long, Long> findSilsonExcludePlanIdMap(PlanCondition cmd) {
        validateQuoteCommand(cmd);

        List<PlanFamily> allFamilies = planReader.loadAllFamilies(cmd.insurerId());
        String planType = policy.resolvePlanType(cmd.insuredList());

        List<PlanFamily> lossFamilies = policy.filterFamilies(allFamilies, planType, false);
        List<PlanFamily> excludeFamilies = policy.filterFamilies(allFamilies, planType, true);

        // 실손제외 planCode → repPlanId 인덱싱
        Map<String, Long> excludeByCode = new HashMap<>();
        for (PlanFamily f : excludeFamilies) {
            excludeByCode.put(f.repPlan().planCode(), f.repPlan().id());
        }

        // 실손포함 repPlanId → 실손제외 repPlanId 매핑 (planCode + "P")
        Map<Long, Long> result = new HashMap<>();
        for (PlanFamily f : lossFamilies) {
            String excludeCode = f.repPlan().planCode() + "P";
            Long excludePlanId = excludeByCode.get(excludeCode);
            if (excludePlanId != null) {
                result.put(f.repPlan().id(), excludePlanId);
            }
        }
        return result;
    }

    // ── 내부 로직 ──

    private void validateQuoteCommand(PlanCondition cmd) {
        if (cmd.insuredList() == null || cmd.insuredList().isEmpty()) {
            throw new CoreException(CoreErrorType.INVALID_QUOTE_REQUEST, "insuredList is empty");
        }
        if (cmd.insurerId() == null) {
            throw new CoreException(CoreErrorType.INVALID_QUOTE_REQUEST, "insurerId is required");
        }
        int repIdx = cmd.representativeIndex() == null ? 0 : cmd.representativeIndex();
        if (repIdx < 0 || repIdx >= cmd.insuredList().size()) {
            throw new CoreException(
                    CoreErrorType.INVALID_QUOTE_REQUEST, "representativeIndex is invalid");
        }
    }
}
