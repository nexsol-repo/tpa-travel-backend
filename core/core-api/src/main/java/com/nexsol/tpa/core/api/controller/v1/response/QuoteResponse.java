
package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.domain.premium.PremiumResult;
import com.nexsol.tpa.core.domain.premium.QuoteResult;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class QuoteResponse {

    private boolean ok;

    private String errCd;

    private String errMsg;

    private String rawErrMsg;

    private Period period;

    private Integer insuredCount;

    private Integer representativeIndex;

    private List<PlanCard> plans;

    @Data
    @Builder
    public static class Period {

        private String insBgnDt;

        private String insEdDt;
    }

    @Data
    @Builder
    public static class PlanCard {

        private Long planId;

        private String planGrpCd;

        private String planCd;

        private String planNm;

        private String planNmRaw;

        private Premium premium;

        private List<InsuredPremium> insuredPremiums;

        private String coverageTitle;

        private List<Coverage> coverages;
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

    @Data
    @Builder
    public static class Premium {

        private long ttPrem;

        private String currency;
    }

    @Data
    @Builder
    public static class SelectedPlan {

        private Long planId;

        private String planGrpCd;

        private String planCd;

        private String planNm;
    }

    @Data
    @Builder
    public static class Coverage {

        private String covCd;

        private String covNm;

        private long insdAmt;

        private String cur;

        private List<CoverageUnit> units;

        private String categoryCode;
    }

    @Data
    @Builder
    public static class CoverageUnit {

        private String ageBandCode;

        private String ageBandLabel;

        private Integer count;

        private Long insdAmt;

        private Long premSum;
    }

    // ── Factory Methods ──

    /**
     * Controller에서 사용하는 주요 팩토리 메서드.
     * 플랜 패밀리 + 보험료 + 담보를 조합하여 QuoteResponse를 생성한다.
     */
    public static QuoteResponse of(
            PlanCondition cmd,
            List<PlanFamily> families,
            Map<Long, PremiumResult> premiumMap,
            Map<Long, List<QuoteResult.DbCoverage>> coverageMap) {
        int repIdx = cmd.representativeIndex() == null ? 0 : cmd.representativeIndex();

        List<PlanCard> cards = new ArrayList<>();
        for (PlanFamily family : families) {
            Long planId = family.repPlan().id();
            PremiumResult premium = premiumMap.get(planId);
            if (premium == null) continue;

            List<QuoteResult.DbCoverage> dbCoverages = coverageMap.getOrDefault(planId, List.of());
            List<Coverage> coverages = assembleCoverages(dbCoverages, premium.coverageAmounts());
            String coverageTitle = buildCoverageTitle(dbCoverages, premium.coverageAmounts());

            cards.add(
                    PlanCard.builder()
                            .planId(planId)
                            .planGrpCd(family.repPlan().planGroupCode())
                            .planCd(family.repPlan().planCode())
                            .planNm(toDisplayName(family.familyName()))
                            .planNmRaw(
                                    family.repPlan().planFullName() != null
                                            ? family.repPlan().planFullName()
                                            : family.repPlan().planName())
                            .premium(
                                    Premium.builder()
                                            .ttPrem(premium.totalPremium())
                                            .currency("KRW")
                                            .build())
                            .insuredPremiums(
                                    premium.insuredPremiums() != null
                                            ? premium.insuredPremiums().stream()
                                                    .map(QuoteResponse::toInsuredPremium)
                                                    .toList()
                                            : List.of())
                            .coverageTitle(coverageTitle)
                            .coverages(coverages)
                            .build());
        }

        if (cards.isEmpty()) {
            return fail("PREM_FAIL", "보험료 산출 결과가 없습니다.", null);
        }

        return success(
                Period.builder().insBgnDt(cmd.insBgnDt()).insEdDt(cmd.insEdDt()).build(),
                repIdx,
                cmd.insuredList().size(),
                cards);
    }

    public static QuoteResponse of(QuoteResult r) {
        return QuoteResponse.builder()
                .ok(r.ok())
                .errCd(r.errCd())
                .errMsg(r.errMsg())
                .rawErrMsg(r.rawErrMsg())
                .period(
                        r.period() != null
                                ? Period.builder()
                                        .insBgnDt(r.period().insBgnDt())
                                        .insEdDt(r.period().insEdDt())
                                        .build()
                                : null)
                .insuredCount(r.insuredCount())
                .representativeIndex(r.representativeIndex())
                .plans(
                        r.plans() != null
                                ? r.plans().stream().map(QuoteResponse::toPlanCard).toList()
                                : List.of())
                .build();
    }

    public static PlanCard toPlanCard(
            PlanFamily family, PremiumResult premium, List<QuoteResult.DbCoverage> dbCoverages) {
        List<Coverage> coverages = assembleCoverages(dbCoverages, premium.coverageAmounts());
        String coverageTitle = buildCoverageTitle(dbCoverages, premium.coverageAmounts());

        return PlanCard.builder()
                .planId(family.repPlan().id())
                .planGrpCd(family.repPlan().planGroupCode())
                .planCd(family.repPlan().planCode())
                .planNm(toDisplayName(family.familyName()))
                .planNmRaw(
                        family.repPlan().planFullName() != null
                                ? family.repPlan().planFullName()
                                : family.repPlan().planName())
                .premium(Premium.builder().ttPrem(premium.totalPremium()).currency("KRW").build())
                .insuredPremiums(
                        premium.insuredPremiums() != null
                                ? premium.insuredPremiums().stream()
                                        .map(QuoteResponse::toInsuredPremium)
                                        .toList()
                                : List.of())
                .coverageTitle(coverageTitle)
                .coverages(coverages)
                .build();
    }

    public static PlanCard toPlanCard(QuoteResult.PlanCard p) {
        List<Coverage> coverages = assembleCoverages(p.dbCoverages(), p.coverageAmounts());
        String coverageTitle = buildCoverageTitle(p.dbCoverages(), p.coverageAmounts());

        return PlanCard.builder()
                .planId(p.planId())
                .planGrpCd(p.planGrpCd())
                .planCd(p.planCd())
                .planNm(p.planNm())
                .planNmRaw(p.planNmRaw())
                .premium(Premium.builder().ttPrem(p.totalPremium()).currency("KRW").build())
                .insuredPremiums(
                        p.insuredPremiums() != null
                                ? p.insuredPremiums().stream()
                                        .map(QuoteResponse::toInsuredPremium)
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

    // ── Presentation: DB 담보 + API 금액 결합 ──

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
        return familyName
                .replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .trim();
    }

    // ── Static Builders ──

    public static QuoteResponse success(
            Period period, int representativeIndex, int insuredCount, List<PlanCard> plans) {
        return QuoteResponse.builder()
                .ok(true)
                .period(period)
                .representativeIndex(representativeIndex)
                .insuredCount(insuredCount)
                .plans(plans)
                .build();
    }

    public static QuoteResponse fail(String errCd, String errMsg, String rawErrMsg) {
        return QuoteResponse.builder()
                .ok(false)
                .errCd(errCd)
                .errMsg(errMsg)
                .rawErrMsg(rawErrMsg)
                .plans(List.of())
                .build();
    }
}
