package com.nexsol.tpa.core.domain.repository;

import java.util.List;
import com.nexsol.tpa.core.domain.repository.projection.TravelPlanCoverageRow;

public interface PlanCoverageRepository {
    List<TravelPlanCoverageRow> findRowsByPlanId(Long planId);
}