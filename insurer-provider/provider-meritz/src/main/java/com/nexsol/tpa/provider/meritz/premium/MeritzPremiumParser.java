package com.nexsol.tpa.provider.meritz.premium;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceQuoteClient.PremiumCommand;
import com.nexsol.tpa.core.domain.plan.AgeBand;
import com.nexsol.tpa.core.domain.plan.QuotePlanPolicy;
import com.nexsol.tpa.core.domain.premium.*;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * 메리츠 API 응답 파서.
 * raw JsonNode → Premium 변환을 담당한다.
 */
@Component
@RequiredArgsConstructor
public class MeritzPremiumParser {

    private static final String DEFAULT_CURRENCY = "KRW";

    private final QuotePlanPolicy policy;

    public Premium parse(JsonNode data, PremiumCommand command) {
        long totalPremium = parseLong(data.path("ttPrem").textValue());
        JsonNode insuredArray = data.path("opapiGnrCoprCtrInspeInfCbcVo");

        List<InsuredPremium> insuredPremiums = buildInsuredPremiums(insuredArray, command);
        Map<String, CoverageAmount> coverageAmounts =
                buildCoverageAmounts(insuredArray, command, command.representativeIndex());

        return new Premium(totalPremium, insuredPremiums, coverageAmounts);
    }

    // ── 피보험자별 보험료 변환 ──

    private List<InsuredPremium> buildInsuredPremiums(JsonNode insuredArray, PremiumCommand cmd) {
        if (!insuredArray.isArray() || insuredArray.isEmpty()) return List.of();

        int count = Math.min(insuredArray.size(), safeSize(cmd.insuredList()));
        return IntStream.range(0, count)
                .mapToObj(i -> toInsuredPremium(i, insuredArray.get(i), cmd.insuredList().get(i)))
                .toList();
    }

    private InsuredPremium toInsuredPremium(
            int index, JsonNode node, PremiumCommand.InsuredPersonCommand insured) {
        return new InsuredPremium(
                index,
                DEFAULT_CURRENCY,
                parseLong(node.path("ppsPrem").textValue()),
                insured.birth(),
                node.path("gndrCd").textValue(),
                node.path("cusNm").textValue(),
                node.path("cusEngNm").textValue());
    }

    // ── 담보별 연령대 집계 ──

    private Map<String, CoverageAmount> buildCoverageAmounts(
            JsonNode insuredArray, PremiumCommand cmd, int repIdx) {
        if (!insuredArray.isArray() || insuredArray.isEmpty()) return Map.of();

        Map<String, Map<String, CoverageAccumulator>> accumulated =
                accumulateCoverageUnits(insuredArray, cmd);
        fillMissingBands(accumulated, resolveRequestedBands(cmd));

        MeritzRepresentativeCoverage representative =
                MeritzRepresentativeCoverage.from(insuredArray, repIdx);
        return toCoverageAmounts(accumulated, representative);
    }

    private Map<String, Map<String, CoverageAccumulator>> accumulateCoverageUnits(
            JsonNode insuredArray, PremiumCommand cmd) {
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

    private Map<String, CoverageAmount> toCoverageAmounts(
            Map<String, Map<String, CoverageAccumulator>> accumulated,
            MeritzRepresentativeCoverage representative) {

        return accumulated.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        representative.buildCoverageAmount(
                                                entry.getKey(), toSortedUnits(entry.getValue()))));
    }

    private List<CoverageUnit> toSortedUnits(Map<String, CoverageAccumulator> bandMap) {
        return bandMap.values().stream()
                .sorted(Comparator.comparingInt(CoverageAccumulator::ageBandMinAge))
                .map(CoverageAccumulator::toUnit)
                .toList();
    }

    // ── 연령대 해석 ──

    private Set<AgeBand> resolveRequestedBands(PremiumCommand cmd) {
        return cmd.insuredList().stream()
                .map(insured -> policy.resolveAgeBand(insured.birth(), cmd.insBgnDt()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private AgeBand resolveAgeBand(PremiumCommand cmd, int insuredIndex) {
        if (cmd.insuredList() == null || cmd.insuredList().size() <= insuredIndex) {
            return null;
        }
        return policy.resolveAgeBand(cmd.insuredList().get(insuredIndex).birth(), cmd.insBgnDt());
    }

    // ── 내부 모델 ──

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

        CoverageUnit toUnit() {
            if (!hasData) {
                return new CoverageUnit(ageBandCode, ageBandLabel, null, null, null);
            }
            return new CoverageUnit(ageBandCode, ageBandLabel, count, insdAmt, premSum);
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
