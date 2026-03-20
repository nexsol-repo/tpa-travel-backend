package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.coverage.PlanCoverage;
import com.nexsol.tpa.core.domain.repository.PlanCoverageRepository;
import com.nexsol.tpa.storage.db.core.repository.JpaPlanCoverageRepository;

@Repository
@RequiredArgsConstructor
public class DefaultPlanCoverageRepository implements PlanCoverageRepository {

    private final JpaPlanCoverageRepository jpaRepository;

    @Override
    public List<PlanCoverage> findByPlanId(Long planId) {
        return jpaRepository.findRowsByPlanId(planId).stream()
                .map(row -> PlanCoverage.builder()
                        .planId(row.getPlanId())
                        .coverageCode(row.getCoverageCode())
                        .coverageName(row.getCoverageName())
                        .displayName(row.getDisplayName())
                        .included(row.isIncluded())
                        .sortOrder(row.getSortOrder())
                        .majorCoverage(row.isMajorCoverage())
                        .titleYn(row.isTitleYn())
                        .categoryCode(row.getCategoryCode())
                        .claimReasonOverride(row.getClaimReasonOverride())
                        .claimContentOverride(row.getClaimContentOverride())
                        .subTitleOverride(row.getSubTitleOverride())
                        .subContentOverride(row.getSubContentOverride())
                        .build())
                .toList();
    }
}