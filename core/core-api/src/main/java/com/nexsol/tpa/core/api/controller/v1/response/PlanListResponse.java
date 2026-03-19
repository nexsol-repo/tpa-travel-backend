package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.domain.premium.PremiumResult;
import com.nexsol.tpa.core.domain.premium.QuoteResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanListResponse {

    private boolean ok;

    private String errCd;

    private String errMsg;

    private Period period;

    private Integer insuredCount;

    private List<PlanSummary> plans;

    @Getter
    @Builder
    public static class Period {

        private String insBgnDt;

        private String insEdDt;
    }

    @Getter
    @Builder
    public static class PlanSummary {

        private Long familyId;

        private Long planId;

        private String planGrpCd;

        private String planCd;

        private String planNm;

        private String planNmRaw;

        private Long silsonExcludePlanId;

        private long totalPremium;

        private String currency;

        private List<RepresentativeCoverage> representativeCoverages;
    }

    @Getter
    @Builder
    public static class RepresentativeCoverage {

        private String covCd;

        private String covNm;

        private long insdAmt;
    }

    // ── Factory Methods ──

    public static PlanListResponse of(
            PlanCondition cmd,
            List<PlanFamily> families,
            Map<Long, PremiumResult> premiumMap,
            Map<Long, List<QuoteResult.DbCoverage>> coverageMap,
            Map<Long, Long> silsonExcludeMap) {

        List<PlanSummary> plans = new ArrayList<>();
        for (PlanFamily family : families) {
            Long planId = family.repPlan().getId();
            PremiumResult premium = premiumMap.get(planId);
            if (premium == null) continue;

            List<QuoteResult.DbCoverage> dbCoverages = coverageMap.getOrDefault(planId, List.of());
            List<RepresentativeCoverage> repCoverages =
                    buildRepresentativeCoverages(dbCoverages, premium.coverageAmounts());

            plans.add(
                    PlanSummary.builder()
                            .familyId(family.familyId())
                            .planId(planId)
                            .planGrpCd(family.repPlan().getPlanGroupCode())
                            .planCd(family.repPlan().getPlanCode())
                            .planNm(toDisplayName(family.familyName()))
                            .planNmRaw(
                                    family.repPlan().getPlanFullName() != null
                                            ? family.repPlan().getPlanFullName()
                                            : family.repPlan().getPlanName())
                            .silsonExcludePlanId(
                                    silsonExcludeMap != null ? silsonExcludeMap.get(planId) : null)
                            .totalPremium(premium.totalPremium())
                            .currency("KRW")
                            .representativeCoverages(repCoverages)
                            .build());
        }

        if (plans.isEmpty()) {
            return fail("PREM_FAIL", "보험료 산출 결과가 없습니다.");
        }

        return success(
                Period.builder().insBgnDt(cmd.insBgnDt()).insEdDt(cmd.insEdDt()).build(),
                cmd.insuredList().size(),
                plans);
    }

    /**
     * titleYn=true인 대표 담보 목록 + 보장금액을 추출한다.
     */
    private static List<RepresentativeCoverage> buildRepresentativeCoverages(
            List<QuoteResult.DbCoverage> dbCoverages,
            Map<String, QuoteResult.CoverageAmount> coverageAmounts) {
        if (dbCoverages == null) return List.of();

        List<RepresentativeCoverage> result = new ArrayList<>();
        for (QuoteResult.DbCoverage row : dbCoverages) {
            if (!row.titleYn()) continue;

            long amount = 0L;
            if (coverageAmounts != null) {
                QuoteResult.CoverageAmount amt = coverageAmounts.get(row.covCd());
                if (amt != null) amount = amt.insdAmt();
            }

            result.add(
                    RepresentativeCoverage.builder()
                            .covCd(row.covCd())
                            .covNm(row.covNm())
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

    public static PlanListResponse success(
            Period period, int insuredCount, List<PlanSummary> plans) {
        return PlanListResponse.builder()
                .ok(true)
                .period(period)
                .insuredCount(insuredCount)
                .plans(plans)
                .build();
    }

    public static PlanListResponse fail(String errCd, String errMsg) {
        return PlanListResponse.builder()
                .ok(false)
                .errCd(errCd)
                .errMsg(errMsg)
                .plans(List.of())
                .build();
    }
}
