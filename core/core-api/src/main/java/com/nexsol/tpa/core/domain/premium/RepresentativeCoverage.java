package com.nexsol.tpa.core.domain.premium;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import tools.jackson.databind.JsonNode;

/**
 * 대표 피보험자의 담보 정보 (개념 객체).
 * 대표자 기준 보험가입금액과 통화 정보를 보유하며,
 * 집계된 연령대별 데이터와 조합해 최종 CoverageAmount를 생성한다.
 */
record RepresentativeCoverage(Map<String, Long> insdAmounts, Map<String, String> currencies) {

    static final RepresentativeCoverage EMPTY = new RepresentativeCoverage(Map.of(), Map.of());
    private static final String DEFAULT_CURRENCY = "KRW";

    /**
     * API 응답 insuredArray에서 대표 피보험자(repIdx)의 담보 정보를 추출한다.
     */
    static RepresentativeCoverage from(JsonNode insuredArray, int repIdx) {
        int idx = Math.max(0, Math.min(repIdx, insuredArray.size() - 1));
        JsonNode repCovs = insuredArray.get(idx).path("opapiGnrCoprCtrQuotCovInfCbcVo");
        if (!repCovs.isArray()) {
            return EMPTY;
        }

        Map<String, Long> insdAmounts = new HashMap<>();
        Map<String, String> currencies = new HashMap<>();

        StreamSupport.stream(repCovs.spliterator(), false)
                .filter(RepresentativeCoverage::hasValidCode)
                .forEach(
                        cov -> {
                            String code = cov.path("covCd").textValue();
                            insdAmounts.put(code, parseLong(cov.path("insdAmt").textValue()));
                            String currency = cov.path("sbcAmtCurCd").textValue();
                            currencies.put(
                                    code,
                                    (currency == null || currency.isBlank())
                                            ? DEFAULT_CURRENCY
                                            : currency);
                        });

        return new RepresentativeCoverage(insdAmounts, currencies);
    }

    /**
     * 자신의 보험가입금액/통화 정보와 집계된 연령대별 units를 조합해 CoverageAmount를 생성한다.
     */
    QuoteResult.CoverageAmount buildCoverageAmount(
            String coverageCode, List<QuoteResult.CoverageUnit> units) {
        return new QuoteResult.CoverageAmount(
                insdAmounts.getOrDefault(coverageCode, 0L),
                currencies.getOrDefault(coverageCode, DEFAULT_CURRENCY),
                units);
    }

    private static boolean hasValidCode(JsonNode coverage) {
        String code = coverage.path("covCd").textValue();
        return code != null && !code.isBlank();
    }

    private static long parseLong(String s) {
        try {
            if (s == null) return 0L;
            return new BigDecimal(s.trim()).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }
}
