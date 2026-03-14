package com.nexsol.tpa.core.domain.premium;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.plan.AgeBand;
import com.nexsol.tpa.core.domain.plan.QuotePlanPolicy;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * 견적 결과 구성 도구 (Tool Layer).
 * raw JsonNode → PremiumResult 변환을 담당한다.
 *
 * <p>처리 순서: 피보험자별 보험료 변환 → 담보별 연령대 집계 → 누락 연령대 보정 → 대표 담보 조합
 */
@Component
@RequiredArgsConstructor
public class QuoteResultComposer {

    private static final String DEFAULT_CURRENCY = "KRW";

    private final QuotePlanPolicy policy;

    // ── public API ──

    /**
     * API 응답 JsonNode를 PremiumResult로 구성한다.
     */
    public PremiumResult compose(JsonNode data, PlanCondition cmd, int repIdx) {
        long totalPremium = parseLong(data.path("ttPrem").textValue());
        JsonNode insuredArray = data.path("opapiGnrCoprCtrInspeInfCbcVo");

        List<QuoteResult.InsuredPremium> insuredPremiums = buildInsuredPremiums(insuredArray, cmd);
        Map<String, QuoteResult.CoverageAmount> coverageAmounts =
                buildCoverageAmounts(insuredArray, cmd, repIdx);

        return new PremiumResult(totalPremium, insuredPremiums, coverageAmounts);
    }

    // ── 피보험자별 보험료 변환 ──

    private List<QuoteResult.InsuredPremium> buildInsuredPremiums(
            JsonNode insuredArray, PlanCondition cmd) {
        if (!insuredArray.isArray() || insuredArray.isEmpty()) return List.of();

        int count = Math.min(insuredArray.size(), safeSize(cmd.insuredList()));
        return IntStream.range(0, count)
                .mapToObj(i -> toInsuredPremium(i, insuredArray.get(i), cmd.insuredList().get(i)))
                .toList();
    }

    private QuoteResult.InsuredPremium toInsuredPremium(
            int index, JsonNode node, PlanCondition.Insured insured) {
        return new QuoteResult.InsuredPremium(
                index,
                DEFAULT_CURRENCY,
                parseLong(node.path("ppsPrem").textValue()),
                insured.birth(),
                node.path("gndrCd").textValue(),
                node.path("cusNm").textValue(),
                node.path("cusEngNm").textValue());
    }

    // ── 담보별 연령대 집계 ──

    private Map<String, QuoteResult.CoverageAmount> buildCoverageAmounts(
            JsonNode insuredArray, PlanCondition cmd, int repIdx) {
        if (!insuredArray.isArray() || insuredArray.isEmpty()) return Map.of();

        Map<String, Map<String, CoverageAccumulator>> accumulated =
                accumulateCoverageUnits(insuredArray, cmd);
        fillMissingBands(accumulated, resolveRequestedBands(cmd));

        RepresentativeCoverage representative = RepresentativeCoverage.from(insuredArray, repIdx);
        return toCoverageAmounts(accumulated, representative);
    }

    private Map<String, Map<String, CoverageAccumulator>> accumulateCoverageUnits(
            JsonNode insuredArray, PlanCondition cmd) {
        Map<String, Map<String, CoverageAccumulator>> accumulated = new HashMap<>();

        IntStream.range(0, insuredArray.size())
                .forEach(
                        i -> {
                            AgeBand band = resolveAgeBand(cmd, i);
                            if (band != null) {
                                accumulateInsuredCoverages(accumulated, insuredArray.get(i), band);
                            }
                        });
        return accumulated;
    }

    private void accumulateInsuredCoverages(
            Map<String, Map<String, CoverageAccumulator>> accumulated,
            JsonNode insured,
            AgeBand band) {

        JsonNode coverages = insured.path("opapiGnrCoprCtrQuotCovInfCbcVo");
        if (!coverages.isArray()) return;

        StreamSupport.stream(coverages.spliterator(), false)
                .filter(this::hasValidCode)
                .forEach(
                        cov ->
                                accumulated
                                        .computeIfAbsent(
                                                cov.path("covCd").textValue(),
                                                k -> new LinkedHashMap<>())
                                        .computeIfAbsent(
                                                band.code(),
                                                k ->
                                                        new CoverageAccumulator(
                                                                band.code(), band.label()))
                                        .add(
                                                parseLong(cov.path("insdAmt").textValue()),
                                                parseLong(cov.path("prem").textValue())));
    }

    // ── 누락 연령대 보정 ──

    private void fillMissingBands(
            Map<String, Map<String, CoverageAccumulator>> accumulated,
            Set<AgeBand> requestedBands) {

        accumulated
                .values()
                .forEach(
                        bandMap ->
                                requestedBands.forEach(
                                        band ->
                                                bandMap.computeIfAbsent(
                                                        band.code(),
                                                        k ->
                                                                new CoverageAccumulator(
                                                                        band.code(),
                                                                        band.label()))));
    }

    // ── 결과 변환 ──

    private Map<String, QuoteResult.CoverageAmount> toCoverageAmounts(
            Map<String, Map<String, CoverageAccumulator>> accumulated,
            RepresentativeCoverage representative) {

        return accumulated.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        representative.buildCoverageAmount(
                                                entry.getKey(), toSortedUnits(entry.getValue()))));
    }

    private List<QuoteResult.CoverageUnit> toSortedUnits(Map<String, CoverageAccumulator> bandMap) {
        return bandMap.values().stream()
                .sorted(Comparator.comparingInt(CoverageAccumulator::ageBandMinAge))
                .map(CoverageAccumulator::toUnit)
                .toList();
    }

    // ── 연령대 해석 ──

    private Set<AgeBand> resolveRequestedBands(PlanCondition cmd) {
        return cmd.insuredList().stream()
                .map(insured -> policy.resolveAgeBand(insured.birth(), cmd.insBgnDt()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private AgeBand resolveAgeBand(PlanCondition cmd, int insuredIndex) {
        if (cmd.insuredList() == null || cmd.insuredList().size() <= insuredIndex) {
            return null;
        }
        return policy.resolveAgeBand(cmd.insuredList().get(insuredIndex).birth(), cmd.insBgnDt());
    }

    // ── 내부 모델 ──

    /**
     * 담보-연령대별 집계 중간 상태.
     * 동일 coverageCode + ageBand 에 대해 count, 최대 보험가입금액, 보험료 합계를 추적한다.
     */
    private static class CoverageAccumulator {
        final String ageBandCode;
        final String ageBandLabel;
        int count = 0;
        long insdAmt = 0;
        long premSum = 0;
        boolean hasData = false;

        CoverageAccumulator(String ageBandCode, String ageBandLabel) {
            this.ageBandCode = ageBandCode;
            this.ageBandLabel = ageBandLabel;
        }

        void add(long insdAmt, long premAmt) {
            this.hasData = true;
            this.count++;
            this.premSum += premAmt;
            if (insdAmt > this.insdAmt) this.insdAmt = insdAmt;
        }

        int ageBandMinAge() {
            AgeBand band = AgeBand.fromCode(ageBandCode);
            return band != null ? band.min() : Integer.MAX_VALUE;
        }

        QuoteResult.CoverageUnit toUnit() {
            if (!hasData) {
                return new QuoteResult.CoverageUnit(ageBandCode, ageBandLabel, null, null, null);
            }
            return new QuoteResult.CoverageUnit(ageBandCode, ageBandLabel, count, insdAmt, premSum);
        }
    }

    // ── 유틸리티 ──

    private boolean hasValidCode(JsonNode coverage) {
        String code = coverage.path("covCd").textValue();
        return code != null && !code.isBlank();
    }

    private int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
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
