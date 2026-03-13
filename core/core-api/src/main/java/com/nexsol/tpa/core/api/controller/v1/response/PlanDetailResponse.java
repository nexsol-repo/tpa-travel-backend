package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
public class PlanDetailResponse {

    private boolean ok;

    private InsuranceInfo insuranceInfo;

    private List<CoverageCategory> coverages;

    @Getter
    @Builder
    public static class InsuranceInfo {

        private Long planId;

        private String planNm;

        private String planNmRaw;

        private String insBgnDt;

        private String insEdDt;

        private long totalPremium;

        private String currency;

        private List<InsuredPremium> insuredPremiums;
    }

    @Getter
    @Builder
    public static class InsuredPremium {

        private Integer index;

        private Long ppsPrem;

        private String birth;

        private String gndrCd;

        private String ageBandCode;

        private String ageBandLabel;
    }

    @Getter
    @Builder
    public static class CoverageCategory {

        private String categoryCode;

        private List<CoverageItem> items;
    }

    @Getter
    @Builder
    public static class CoverageItem {

        private String covCd;

        private String covNm;

        private long insdAmt;

        private String cur;

        private boolean titleYn;
    }

    // ── Factory Methods ──

    public static PlanDetailResponse of(
            PlanCondition cmd,
            PlanFamily family,
            PremiumResult premium,
            List<QuoteResult.DbCoverage> dbCoverages) {

        List<InsuredPremium> insuredPremiums =
                premium.insuredPremiums() != null
                        ? premium.insuredPremiums().stream()
                                .map(
                                        ip ->
                                                InsuredPremium.builder()
                                                        .index(ip.index())
                                                        .ppsPrem(ip.ppsPrem())
                                                        .birth(ip.birth())
                                                        .gndrCd(ip.gndrCd())
                                                        .build())
                                .toList()
                        : List.of();

        InsuranceInfo info =
                InsuranceInfo.builder()
                        .planId(family.repPlan().getId())
                        .planNm(toDisplayName(family.familyName()))
                        .planNmRaw(
                                family.repPlan().getPlanFullName() != null
                                        ? family.repPlan().getPlanFullName()
                                        : family.repPlan().getPlanName())
                        .insBgnDt(cmd.insBgnDt())
                        .insEdDt(cmd.insEdDt())
                        .totalPremium(premium.totalPremium())
                        .currency("KRW")
                        .insuredPremiums(insuredPremiums)
                        .build();

        List<CoverageCategory> coverages =
                buildCoverageCategories(dbCoverages, premium.coverageAmounts());

        return PlanDetailResponse.builder()
                .ok(true)
                .insuranceInfo(info)
                .coverages(coverages)
                .build();
    }

    private static List<CoverageCategory> buildCoverageCategories(
            List<QuoteResult.DbCoverage> dbCoverages,
            Map<String, QuoteResult.CoverageAmount> coverageAmounts) {
        if (dbCoverages == null) return List.of();

        Map<String, List<CoverageItem>> grouped = new LinkedHashMap<>();
        for (QuoteResult.DbCoverage row : dbCoverages) {
            long amount = 0L;
            String cur = "KRW";
            if (coverageAmounts != null) {
                QuoteResult.CoverageAmount amt = coverageAmounts.get(row.covCd());
                if (amt != null) {
                    amount = amt.insdAmt();
                    cur = amt.currency();
                }
            }

            String category = row.categoryCode() != null ? row.categoryCode() : "기타";
            grouped.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(
                            CoverageItem.builder()
                                    .covCd(row.covCd())
                                    .covNm(row.covNm())
                                    .insdAmt(amount)
                                    .cur(cur)
                                    .titleYn(row.titleYn())
                                    .build());
        }

        List<CoverageCategory> result = new ArrayList<>();
        for (var e : grouped.entrySet()) {
            result.add(
                    CoverageCategory.builder()
                            .categoryCode(e.getKey())
                            .items(e.getValue())
                            .build());
        }
        return result;
    }

    private static String toDisplayName(String familyName) {
        if (familyName == null) return null;
        return familyName.replace("플랜A", "플랜").replace("플랜B", "플랜").replace(" 실손제외", "").trim();
    }
}
