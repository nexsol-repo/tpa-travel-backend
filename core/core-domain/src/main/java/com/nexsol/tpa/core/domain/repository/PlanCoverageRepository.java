package com.nexsol.tpa.core.domain.repository;

import java.util.List;

import com.nexsol.tpa.core.domain.coverage.PlanCoverage;

public interface PlanCoverageRepository {
    List<PlanCoverage> findByPlanId(Long planId);
}
