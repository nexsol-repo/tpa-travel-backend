package com.nexsol.tpa.core.domain.premium;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.plan.AgeBand;
import com.nexsol.tpa.core.domain.plan.QuotePlanPolicy;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * 보험료 API 응답 집계 도구 (Tool Layer).
 * raw JsonNode → PremiumResult 변환을 담당한다.
 */
@Component
@RequiredArgsConstructor
public class PremiumAggregator {

    private final QuotePlanPolicy policy;

    /**
     * API 응답 JsonNode를 PremiumResult로 집계한다.
     */
    public PremiumResult aggregate(JsonNode data, PlanCondition cmd, int repIdx) {
        long totalPremium = parseLong(data.path("ttPrem").textValue());
        JsonNode insuredArray = data.path("opapiGnrCoprCtrInspeInfCbcVo");

        List<QuoteResult.InsuredPremium> insuredPremiums = buildInsuredPremiums(insuredArray, cmd);
        Set<AgeBand> requestedBands = resolveRequestedBands(cmd);
        Map<String, QuoteResult.CoverageAmount> coverageAmounts =
                buildCoverageAmounts(insuredArray, cmd, repIdx, requestedBands);

        return new PremiumResult(totalPremium, insuredPremiums, coverageAmounts);
    }

    // ── 피보험자별 보험료 ──

    private List<QuoteResult.InsuredPremium> buildInsuredPremiums(
            JsonNode insuredArray, PlanCondition cmd) {
        if (!insuredArray.isArray() || insuredArray.isEmpty()) return List.of();

        int n =
                Math.min(
                        insuredArray.size(),
                        cmd.insuredList() == null ? 0 : cmd.insuredList().size());
        List<QuoteResult.InsuredPremium> out = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            JsonNode m = insuredArray.get(i);
            PlanCondition.Insured r = cmd.insuredList().get(i);
            out.add(
                    new QuoteResult.InsuredPremium(
                            i,
                            "KRW",
                            parseLong(m.path("ppsPrem").textValue()),
                            r.birth(),
                            m.path("gndrCd").textValue(),
                            m.path("cusNm").textValue(),
                            m.path("cusEngNm").textValue()));
        }
        return List.copyOf(out);
    }

    // ── 담보별 금액 집계 ──

    private Map<String, QuoteResult.CoverageAmount> buildCoverageAmounts(
            JsonNode insuredArray, PlanCondition cmd, int repIdx, Set<AgeBand> requestedBands) {
        if (!insuredArray.isArray() || insuredArray.isEmpty()) return Map.of();

        Map<String, Map<String, UnitAccumulator>> accumulated =
                accumulateCoverageUnits(insuredArray, cmd);
        fillMissingBands(accumulated, requestedBands);
        RepresentativeCoverage representative = extractRepresentativeCoverage(insuredArray, repIdx);
        return toCoverageAmounts(accumulated, representative);
    }

    private Map<String, Map<String, UnitAccumulator>> accumulateCoverageUnits(
            JsonNode insuredArray, PlanCondition cmd) {
        Map<String, Map<String, UnitAccumulator>> accumulated = new HashMap<>();

        for (int i = 0; i < insuredArray.size(); i++) {
            AgeBand band = resolveAgeBand(cmd, i);
            if (band == null) continue;

            accumulateInsuredCoverages(accumulated, insuredArray.get(i), band);
        }
        return accumulated;
    }

    private void accumulateInsuredCoverages(
            Map<String, Map<String, UnitAccumulator>> accumulated, JsonNode insured, AgeBand band) {
        JsonNode coverages = insured.path("opapiGnrCoprCtrQuotCovInfCbcVo");
        if (!coverages.isArray()) return;

        for (JsonNode coverage : coverages) {
            String coverageCode = coverage.path("covCd").textValue();
            if (coverageCode == null || coverageCode.isBlank()) continue;

            accumulated
                    .computeIfAbsent(coverageCode, k -> new LinkedHashMap<>())
                    .computeIfAbsent(
                            band.code(), k -> new UnitAccumulator(band.code(), band.label()))
                    .add(
                            parseLong(coverage.path("insdAmt").textValue()),
                            parseLong(coverage.path("prem").textValue()));
        }
    }

    private void fillMissingBands(
            Map<String, Map<String, UnitAccumulator>> accumulated, Set<AgeBand> requestedBands) {

        for (var entry : accumulated.entrySet()) {
            for (AgeBand band : requestedBands) {
                entry.getValue()
                        .computeIfAbsent(
                                band.code(), k -> new UnitAccumulator(band.code(), band.label()));
            }
        }
    }

    private RepresentativeCoverage extractRepresentativeCoverage(
            JsonNode insuredArray, int repIdx) {
        Map<String, Long> insdAmounts = new HashMap<>();
        Map<String, String> currencies = new HashMap<>();

        int idx = Math.max(0, Math.min(repIdx, insuredArray.size() - 1));
        JsonNode repCovs = insuredArray.get(idx).path("opapiGnrCoprCtrQuotCovInfCbcVo");
        if (!repCovs.isArray()) {
            return new RepresentativeCoverage(insdAmounts, currencies);
        }

        for (JsonNode coverage : repCovs) {
            String coverageCode = coverage.path("covCd").textValue();
            if (coverageCode == null || coverageCode.isBlank()) continue;

            insdAmounts.put(coverageCode, parseLong(coverage.path("insdAmt").textValue()));
            String currency = coverage.path("sbcAmtCurCd").textValue();
            currencies.put(
                    coverageCode, (currency == null || currency.isBlank()) ? "KRW" : currency);
        }
        return new RepresentativeCoverage(insdAmounts, currencies);
    }

    private Map<String, QuoteResult.CoverageAmount> toCoverageAmounts(
            Map<String, Map<String, UnitAccumulator>> accumulated,
            RepresentativeCoverage representative) {

        Map<String, QuoteResult.CoverageAmount> result = new HashMap<>();
        for (var entry : accumulated.entrySet()) {
            String coverageCode = entry.getKey();
            List<QuoteResult.CoverageUnit> units =
                    entry.getValue().values().stream()
                            .sorted(
                                    Comparator.comparingInt(
                                            u -> {
                                                AgeBand band = AgeBand.fromCode(u.ageBandCode);
                                                return band != null
                                                        ? band.min()
                                                        : Integer.MAX_VALUE;
                                            }))
                            .map(UnitAccumulator::toUnit)
                            .toList();

            result.put(
                    coverageCode,
                    new QuoteResult.CoverageAmount(
                            representative.insdAmounts.getOrDefault(coverageCode, 0L),
                            representative.currencies.getOrDefault(coverageCode, "KRW"),
                            units));
        }
        return result;
    }

    private Set<AgeBand> resolveRequestedBands(PlanCondition cmd) {
        Set<AgeBand> bands = new LinkedHashSet<>();
        for (PlanCondition.Insured insured : cmd.insuredList()) {
            AgeBand band = policy.resolveAgeBand(insured.birth(), cmd.insBgnDt());
            if (band != null) bands.add(band);
        }
        return bands;
    }

    private AgeBand resolveAgeBand(PlanCondition cmd, int insuredIndex) {
        if (cmd.insuredList() == null || cmd.insuredList().size() <= insuredIndex) {
            return null;
        }
        return policy.resolveAgeBand(cmd.insuredList().get(insuredIndex).birth(), cmd.insBgnDt());
    }

    private record RepresentativeCoverage(
            Map<String, Long> insdAmounts, Map<String, String> currencies) {}

    private static class UnitAccumulator {
        final String ageBandCode;
        final String ageBandLabel;
        int count = 0;
        long insdAmt = 0;
        long premSum = 0;
        boolean hasData = false;

        UnitAccumulator(String ageBandCode, String ageBandLabel) {
            this.ageBandCode = ageBandCode;
            this.ageBandLabel = ageBandLabel;
        }

        void add(long insdAmt, long premAmt) {
            this.hasData = true;
            this.count++;
            this.premSum += premAmt;
            if (insdAmt > this.insdAmt) this.insdAmt = insdAmt;
        }

        QuoteResult.CoverageUnit toUnit() {
            if (!hasData) {
                return new QuoteResult.CoverageUnit(ageBandCode, ageBandLabel, null, null, null);
            }
            return new QuoteResult.CoverageUnit(ageBandCode, ageBandLabel, count, insdAmt, premSum);
        }
    }

    private long parseLong(String s) {
        try {
            if (s == null) return 0L;
            return new BigDecimal(s.trim()).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }
}
