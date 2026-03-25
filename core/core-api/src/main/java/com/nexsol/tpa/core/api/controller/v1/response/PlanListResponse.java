package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nexsol.tpa.core.domain.coverage.FamilyCoverageDetail;
import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.premium.CoverageAmount;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.domain.premium.Premium;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanListResponse {

    private PlanPeriodResponse period;

    private Integer insuredCount;

    private List<PlanSummaryResponse> plans;

    // ── Factory Methods ──

    public static PlanListResponse of(
            PlanCondition cmd,
            List<PlanFamily> families,
            Map<Long, Premium> premiumMap,
            Map<Long, List<FamilyCoverageDetail>> coverageMap,
            Map<Long, Long> silsonExcludeMap) {

        List<PlanSummaryResponse> plans = new ArrayList<>();
        for (PlanFamily family : families) {
            Long planId = family.repPlan().id();
            Premium premium = premiumMap.get(planId);
            if (premium == null) continue;

            List<FamilyCoverageDetail> coverages =
                    coverageMap.getOrDefault(family.familyId(), List.of());
            List<RepresentativeCoverageResponse> repCoverages =
                    buildRepresentativeCoverages(coverages, premium.coverageAmounts());

            plans.add(
                    PlanSummaryResponse.builder()
                            .familyId(family.familyId())
                            .planId(planId)
                            .planGrpCd(family.repPlan().planGroupCode())
                            .planCd(family.repPlan().planCode())
                            .planNm(toDisplayName(family.familyName()))
                            .planNmRaw(
                                    family.repPlan().planFullName() != null
                                            ? family.repPlan().planFullName()
                                            : family.repPlan().planName())
                            .silsonExcludePlanId(
                                    silsonExcludeMap != null ? silsonExcludeMap.get(planId) : null)
                            .totalPremium(premium.totalPremium())
                            .currency("KRW")
                            .representativeCoverages(repCoverages)
                            .build());
        }

        if (plans.isEmpty()) {
            throw new CoreException(CoreErrorType.PREMIUM_CALCULATION_FAILED, "보험료 산출 결과가 없습니다.");
        }

        return PlanListResponse.builder()
                .period(
                        PlanPeriodResponse.builder()
                                .insBgnDt(cmd.insBgnDt())
                                .insEdDt(cmd.insEdDt())
                                .build())
                .insuredCount(cmd.insuredList().size())
                .plans(plans)
                .build();
    }

    private static List<RepresentativeCoverageResponse> buildRepresentativeCoverages(
            List<FamilyCoverageDetail> coverages, Map<String, CoverageAmount> coverageAmounts) {
        if (coverages == null) return List.of();

        List<RepresentativeCoverageResponse> result = new ArrayList<>();
        for (FamilyCoverageDetail detail : coverages) {
            if (!detail.featured()) continue;

            long amount = 0L;
            if (coverageAmounts != null) {
                CoverageAmount amt = coverageAmounts.get(detail.coverageCode());
                if (amt != null) amount = amt.insdAmt();
            }

            result.add(
                    RepresentativeCoverageResponse.builder()
                            .covCd(detail.coverageCode())
                            .covNm(detail.resolvedName())
                            .insdAmt(amount)
                            .build());
        }
        return result;
    }

    private static String toDisplayName(String familyName) {
        if (familyName == null) return null;
        return familyName
                .replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .trim();
    }
}
