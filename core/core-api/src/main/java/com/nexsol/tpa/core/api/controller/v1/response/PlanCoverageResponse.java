package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.premium.PremiumResult;
import com.nexsol.tpa.core.domain.premium.QuoteResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanCoverageResponse {

    private Long planId;

    private String planGrpCd;

    private String planCd;

    private String planNm;

    private String planNmRaw;

    private Premium premium;

    private List<InsuredPremium> insuredPremiums;

    private String coverageTitle;

    private List<Coverage> coverages;

    @Getter
    @Builder
    public static class Premium {

        private long ttPrem;

        private String currency;
    }

    @Getter
    @Builder
    public static class InsuredPremium {

        private Integer index;

        private String currency;

        private Long ppsPrem;

        private String birth;

        private String gndrCd;

        private String cusNm;

        private String cusEngNm;

        private String ageBandCode;

        private String ageBandLabel;
    }

    @Getter
    @Builder
    public static class Coverage {

        private String covCd;

        private String covNm;

        private long insdAmt;

        private String cur;

        private List<CoverageUnit> units;

        private String categoryCode;
    }

    @Getter
    @Builder
    public static class CoverageUnit {

        private String ageBandCode;

        private String ageBandLabel;

        private Integer count;

        private Long insdAmt;

        private Long premSum;
    }

    // ── Factory Method ──

    public static PlanCoverageResponse of(
            PlanFamily family, PremiumResult premium, List<QuoteResult.DbCoverage> dbCoverages) {
        List<Coverage> coverages = assembleCoverages(dbCoverages, premium.coverageAmounts());
        String coverageTitle = buildCoverageTitle(dbCoverages, premium.coverageAmounts());

        return PlanCoverageResponse.builder()
                .planId(family.repPlan().getId())
                .planGrpCd(family.repPlan().getPlanGroupCode())
                .planCd(family.repPlan().getPlanCode())
                .planNm(toDisplayName(family.familyName()))
                .planNmRaw(
                        family.repPlan().getPlanFullName() != null
                                ? family.repPlan().getPlanFullName()
                                : family.repPlan().getPlanName())
                .premium(Premium.builder().ttPrem(premium.totalPremium()).currency("KRW").build())
                .insuredPremiums(
                        premium.insuredPremiums() != null
                                ? premium.insuredPremiums().stream()
                                        .map(PlanCoverageResponse::toInsuredPremium)
                                        .toList()
                                : List.of())
                .coverageTitle(coverageTitle)
                .coverages(coverages)
                .build();
    }

    private static InsuredPremium toInsuredPremium(QuoteResult.InsuredPremium ip) {
        return InsuredPremium.builder()
                .index(ip.index())
                .currency(ip.currency())
                .ppsPrem(ip.ppsPrem())
                .birth(ip.birth())
                .gndrCd(ip.gndrCd())
                .cusNm(ip.cusNm())
                .cusEngNm(ip.cusEngNm())
                .build();
    }

    // ── Presentation ──

    private static List<Coverage> assembleCoverages(
            List<QuoteResult.DbCoverage> dbCoverages,
            Map<String, QuoteResult.CoverageAmount> coverageAmounts) {
        if (dbCoverages == null) return List.of();

        List<Coverage> result = new ArrayList<>();
        for (QuoteResult.DbCoverage row : dbCoverages) {
            QuoteResult.CoverageAmount amt =
                    coverageAmounts != null ? coverageAmounts.get(row.covCd()) : null;

            if (amt == null) {
                result.add(
                        Coverage.builder()
                                .covCd(row.covCd())
                                .covNm(row.covNm())
                                .insdAmt(0L)
                                .cur("KRW")
                                .units(List.of())
                                .categoryCode(row.categoryCode())
                                .build());
            } else {
                result.add(
                        Coverage.builder()
                                .covCd(row.covCd())
                                .covNm(row.covNm())
                                .insdAmt(amt.insdAmt())
                                .cur(amt.currency())
                                .units(
                                        amt.units() != null
                                                ? amt.units().stream()
                                                        .map(
                                                                u ->
                                                                        CoverageUnit.builder()
                                                                                .ageBandCode(
                                                                                        u
                                                                                                .ageBandCode())
                                                                                .ageBandLabel(
                                                                                        u
                                                                                                .ageBandLabel())
                                                                                .count(u.count())
                                                                                .insdAmt(
                                                                                        u.insdAmt())
                                                                                .premSum(
                                                                                        u.premSum())
                                                                                .build())
                                                        .toList()
                                                : List.of())
                                .categoryCode(row.categoryCode())
                                .build());
            }
        }
        return result;
    }

    private static String buildCoverageTitle(
            List<QuoteResult.DbCoverage> dbCoverages,
            Map<String, QuoteResult.CoverageAmount> coverageAmounts) {
        if (dbCoverages == null || coverageAmounts == null) return null;

        List<String> parts = new ArrayList<>();
        for (QuoteResult.DbCoverage row : dbCoverages) {
            if (!row.titleYn()) continue;

            QuoteResult.CoverageAmount amt = coverageAmounts.get(row.covCd());
            long amount = (amt != null) ? amt.insdAmt() : 0L;
            if (amount <= 0) continue;

            parts.add(row.covNm() + " " + formatWonShort(amount));
        }
        return parts.isEmpty() ? null : "보장금액 : " + String.join(" / ", parts);
    }

    private static String formatWonShort(long amount) {
        long eok = amount / 100_000_000L;
        long man = (amount % 100_000_000L) / 10_000L;
        if (eok > 0 && man == 0) return eok + "억원";
        if (eok > 0) return eok + "억" + String.format("%,d", man) + "만원";
        if (man > 0) return String.format("%,d", man) + "만원";
        return String.format("%,d", amount) + "원";
    }

    private static String toDisplayName(String familyName) {
        if (familyName == null) return null;
        return familyName.replace("플랜A", "플랜").replace("플랜B", "플랜").replace(" 실손제외", "").trim();
    }
}
