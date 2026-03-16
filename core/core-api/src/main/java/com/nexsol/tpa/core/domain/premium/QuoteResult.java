package com.nexsol.tpa.core.domain.premium;

import java.util.List;
import java.util.Map;

public record QuoteResult(
        boolean ok,
        String errCd,
        String errMsg,
        String rawErrMsg,
        Period period,
        Integer insuredCount,
        Integer representativeIndex,
        List<PlanCard> plans) {

    public record Period(String insBgnDt, String insEdDt) {}

    /** 플랜 카드: 비즈니스 결과(raw). 표현 조합은 QuoteResponse 에서 처리. */
    public record PlanCard(
            Long planId,
            String planGrpCd,
            String planCd,
            String planNm,
            String planNmRaw,
            long totalPremium,
            List<InsuredPremium> insuredPremiums,
            Map<String, CoverageAmount> coverageAmounts,
            List<DbCoverage> dbCoverages) {}

    public record InsuredPremium(
            Integer index,
            String currency,
            Long ppsPrem,
            String birth,
            String gndrCd,
            String cusNm,
            String cusEngNm) {}

    /** API003 응답에서 파싱된 담보별 금액 */
    public record CoverageAmount(long insdAmt, String currency, List<CoverageUnit> units) {}

    public record CoverageUnit(
            String ageBandCode, String ageBandLabel, Integer count, Long insdAmt, Long premSum) {}

    /** DB 담보 정보 (표현 조합용) */
    public record DbCoverage(String covCd, String covNm, boolean titleYn, String categoryCode) {}

    public static QuoteResult success(
            Period period, int representativeIndex, int insuredCount, List<PlanCard> plans) {
        return new QuoteResult(
                true, null, null, null, period, insuredCount, representativeIndex, plans);
    }

    public static QuoteResult fail(String errCd, String errMsg, String rawErrMsg) {
        return new QuoteResult(false, errCd, errMsg, rawErrMsg, null, null, null, List.of());
    }
}
