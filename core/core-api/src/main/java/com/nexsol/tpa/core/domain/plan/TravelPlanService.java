package com.nexsol.tpa.core.domain.plan;

import java.util.List;

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
        List<PlanFamily> families = policy.filterFamilies(allFamilies, planType);

        if (families.isEmpty()) {
            throw new CoreApiException(
                    CoreApiErrorType.QUOTE_PLAN_NOT_FOUND, "플랜타입(" + planType + ") 기준 플랜 없음");
        }
        return families;
    }

    /**
     * planId가 속한 일반 견적 패밀리 1개를 조회한다.
     * 플랜 상세(보험료·보장내용) 조회에 사용.
     */
    public PlanFamily findFamilyByPlanId(PlanCondition cmd, Long planId) {
        List<PlanFamily> families = findQuoteFamilies(cmd);
        return families.stream()
                .filter(f -> f.plans().stream().anyMatch(p -> p.getId().equals(planId)))
                .findFirst()
                .orElseThrow(
                        () ->
                                new CoreApiException(
                                        CoreApiErrorType.QUOTE_PLAN_NOT_FOUND, "planId=" + planId));
    }

    /**
     * 실손제외 대상 패밀리를 조회한다.
     * 선택된 planId → 대응하는 실손제외 패밀리 1개 반환.
     */
    public PlanFamily findSilsonExcludeFamily(PlanCondition cmd, Long planId) {
        validateQuoteCommand(cmd);

        List<PlanFamily> allFamilies = planReader.loadAllFamilies(cmd.insurerId());
        return policy.resolveSilsonExcludeFamily(allFamilies, planId);
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
