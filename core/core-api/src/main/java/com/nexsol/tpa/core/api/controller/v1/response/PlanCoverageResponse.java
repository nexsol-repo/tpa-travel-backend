package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nexsol.tpa.core.domain.coverage.FamilyCoverageDetail;
import com.nexsol.tpa.core.domain.plan.AgeBand;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.premium.CoverageAmount;
import com.nexsol.tpa.core.domain.premium.Premium;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanCoverageResponse {

    private Long familyId;

    private Long planId;

    private String planGrpCd;

    private String planCd;

    private String planNm;

    private String planNmRaw;

    private PremiumSummary premium;

    private List<InsuredPremiumResponse> insuredPremiums;

    private String coverageTitle;

    private List<CoverageSectionResponse> sections;

    // ── Factory Method ──

    public static PlanCoverageResponse of(
            PlanFamily family, Premium premium, List<FamilyCoverageDetail> coverageDetails) {
        List<CoverageSectionResponse> sections =
                assembleSections(coverageDetails, premium.coverageAmounts());
        String coverageTitle = buildCoverageTitle(coverageDetails, premium.coverageAmounts());

        return PlanCoverageResponse.builder()
                .familyId(family.familyId())
                .planId(family.repPlan().id())
                .planGrpCd(family.repPlan().planGroupCode())
                .planCd(family.repPlan().planCode())
                .planNm(toDisplayName(family.familyName()))
                .planNmRaw(
                        family.repPlan().planFullName() != null
                                ? family.repPlan().planFullName()
                                : family.repPlan().planName())
                .premium(
                        PremiumSummary.builder()
                                .ttPrem(premium.totalPremium())
                                .currency("KRW")
                                .build())
                .insuredPremiums(
                        premium.insuredPremiums() != null
                                ? premium.insuredPremiums().stream()
                                        .map(ip -> toInsuredPremium(ip, family.plans()))
                                        .toList()
                                : List.of())
                .coverageTitle(coverageTitle)
                .sections(sections)
                .build();
    }

    private static InsuredPremiumResponse toInsuredPremium(
            com.nexsol.tpa.core.domain.premium.InsuredPremium ip, List<InsurancePlan> familyPlans) {
        Long planId = resolvePlanId(ip.birth(), familyPlans);
        return InsuredPremiumResponse.builder()
                .index(ip.index())
                .planId(planId)
                .currency(ip.currency())
                .ppsPrem(ip.ppsPrem())
                .birth(ip.birth())
                .gndrCd(ip.gndrCd())
                .cusNm(ip.cusNm())
                .cusEngNm(ip.cusEngNm())
                .build();
    }

    private static Long resolvePlanId(String birth, List<InsurancePlan> familyPlans) {
        if (birth == null || familyPlans == null) return null;
        AgeBand band = AgeBand.fromAge(calcAgeFromToday(birth));
        if (band == null) return null;
        return familyPlans.stream()
                .filter(p -> p.ageGroupId() != null && p.ageGroupId() == band.ageGroupId())
                .map(InsurancePlan::id)
                .findFirst()
                .orElse(null);
    }

    private static int calcAgeFromToday(String birthYmd) {
        java.time.LocalDate birth =
                java.time.LocalDate.parse(
                        birthYmd, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
    }

    // ── Presentation ──

    private static List<CoverageSectionResponse> assembleSections(
            List<FamilyCoverageDetail> coverageDetails,
            Map<String, CoverageAmount> coverageAmounts) {
        if (coverageDetails == null) return List.of();

        Map<String, List<CoverageAmountResponse>> grouped = new LinkedHashMap<>();
        Map<String, String> sectionNames = new LinkedHashMap<>();

        for (FamilyCoverageDetail detail : coverageDetails) {
            String sectionCode = detail.sectionCode();
            sectionNames.putIfAbsent(sectionCode, detail.sectionName());

            CoverageAmount amt =
                    coverageAmounts != null ? coverageAmounts.get(detail.coverageCode()) : null;

            CoverageAmountResponse item =
                    CoverageAmountResponse.builder()
                            .covCd(detail.coverageCode())
                            .covNm(detail.resolvedName())
                            .coverageName(detail.coverage().coverageName())
                            .insdAmt(amt != null ? amt.insdAmt() : 0L)
                            .cur(amt != null ? amt.currency() : "KRW")
                            .units(
                                    amt != null && amt.units() != null
                                            ? amt.units().stream()
                                                    .map(
                                                            u ->
                                                                    CoverageUnitResponse.builder()
                                                                            .ageBandCode(
                                                                                    u.ageBandCode())
                                                                            .ageBandLabel(
                                                                                    u
                                                                                            .ageBandLabel())
                                                                            .count(u.count())
                                                                            .insdAmt(u.insdAmt())
                                                                            .premSum(u.premSum())
                                                                            .build())
                                                    .toList()
                                            : List.of())
                            .build();

            grouped.computeIfAbsent(sectionCode, k -> new ArrayList<>()).add(item);
        }

        return grouped.entrySet().stream()
                .map(
                        entry ->
                                CoverageSectionResponse.builder()
                                        .sectionCode(entry.getKey())
                                        .sectionName(sectionNames.get(entry.getKey()))
                                        .coverages(entry.getValue())
                                        .build())
                .toList();
    }

    private static String buildCoverageTitle(
            List<FamilyCoverageDetail> coverageDetails,
            Map<String, CoverageAmount> coverageAmounts) {
        if (coverageDetails == null || coverageAmounts == null) return null;

        List<String> parts = new ArrayList<>();
        for (FamilyCoverageDetail detail : coverageDetails) {
            if (!detail.featured()) continue;

            CoverageAmount amt = coverageAmounts.get(detail.coverageCode());
            long amount = (amt != null) ? amt.insdAmt() : 0L;
            if (amount <= 0) continue;

            parts.add(detail.resolvedName() + " " + formatWonShort(amount));
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
        return familyName
                .replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .trim();
    }
}
