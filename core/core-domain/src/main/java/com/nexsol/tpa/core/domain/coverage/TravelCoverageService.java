package com.nexsol.tpa.core.domain.coverage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.plan.TravelPlanReader;
import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.premium.QuoteResult;
import com.nexsol.tpa.core.domain.repository.projection.TravelPlanCoverageRow;

import lombok.RequiredArgsConstructor;

/**
 * 담보 조회 비즈니스 서비스 (Business Layer).
 * TravelPlanReader를 통해 DB 담보를 조회하고 도메인 record로 변환한다.
 */
@Service
@RequiredArgsConstructor
public class TravelCoverageService {

    private final TravelPlanReader planReader;

    /**
     * 패밀리 목록의 담보를 일괄 조회한다.
     */
    public Map<Long, List<QuoteResult.DbCoverage>> findCoveragesForFamilies(
            List<PlanFamily> families) {
        Map<Long, List<QuoteResult.DbCoverage>> map = new LinkedHashMap<>();
        for (PlanFamily family : families) {
            map.put(family.repPlan().getId(), findCoverages(family.repPlan().getId()));
        }
        return map;
    }

    /**
     * 플랜의 DB 담보 목록을 조회하여 도메인 record로 변환한다.
     */
    public List<QuoteResult.DbCoverage> findCoverages(Long planId) {
        return planReader.loadCoverages(planId).stream()
                .filter(TravelPlanCoverageRow::isIncluded)
                .map(
                        row ->
                                new QuoteResult.DbCoverage(
                                        row.getCoverageCode(),
                                        (row.getDisplayName() != null
                                                        && !row.getDisplayName().isBlank())
                                                ? row.getDisplayName()
                                                : row.getCoverageName(),
                                        row.isTitleYn(),
                                        row.getCategoryCode()))
                .toList();
    }
}
