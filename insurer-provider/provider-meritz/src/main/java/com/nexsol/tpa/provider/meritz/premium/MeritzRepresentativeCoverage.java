package com.nexsol.tpa.provider.meritz.premium;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.nexsol.tpa.core.domain.premium.CoverageAmount;
import com.nexsol.tpa.core.domain.premium.CoverageUnit;

import tools.jackson.databind.JsonNode;

/**
 * 메리츠 API 응답에서 대표 피보험자의 담보 정보를 추출.
 */
record MeritzRepresentativeCoverage(Map<String, Long> insdAmounts, Map<String, String> currencies) {

    static final MeritzRepresentativeCoverage EMPTY =
            new MeritzRepresentativeCoverage(Map.of(), Map.of());
    private static final String DEFAULT_CURRENCY = "KRW";

    static MeritzRepresentativeCoverage from(JsonNode insuredArray, int repIdx) {
        int idx = Math.max(0, Math.min(repIdx, insuredArray.size() - 1));
        JsonNode repCovs = insuredArray.get(idx).path("opapiGnrCoprCtrQuotCovInfCbcVo");
        if (!repCovs.isArray()) {
            return EMPTY;
        }

        Map<String, Long> insdAmounts = new HashMap<>();
        Map<String, String> currencies = new HashMap<>();

        StreamSupport.stream(repCovs.spliterator(), false)
                .filter(MeritzRepresentativeCoverage::hasValidCode)
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

        return new MeritzRepresentativeCoverage(insdAmounts, currencies);
    }

    CoverageAmount buildCoverageAmount(String coverageCode, List<CoverageUnit> units) {
        return new CoverageAmount(
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
