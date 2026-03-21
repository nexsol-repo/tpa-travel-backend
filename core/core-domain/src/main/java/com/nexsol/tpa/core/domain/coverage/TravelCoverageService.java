package com.nexsol.tpa.core.domain.coverage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.core.domain.premium.QuoteResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelCoverageService {

    private final PlanReader planReader;

    public Map<Long, List<QuoteResult.DbCoverage>> findCoveragesForFamilies(
            List<PlanFamily> families) {
        Map<Long, List<QuoteResult.DbCoverage>> map = new LinkedHashMap<>();
        for (PlanFamily family : families) {
            map.put(family.repPlan().id(), findCoverages(family.repPlan().id()));
        }
        return map;
    }

    public List<QuoteResult.DbCoverage> findCoverages(Long planId) {
        return planReader.loadCoverages(planId).stream()
                .filter(PlanCoverage::included)
                .map(
                        pc ->
                                new QuoteResult.DbCoverage(
                                        pc.coverageCode(),
                                        (pc.displayName() != null && !pc.displayName().isBlank())
                                                ? pc.displayName()
                                                : pc.coverageName(),
                                        pc.titleYn(),
                                        pc.categoryCode()))
                .toList();
    }
}
