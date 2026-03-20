package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.PlanCoverageRepository;
import com.nexsol.tpa.storage.db.core.repository.JpaPlanCoverageRepository;
import com.nexsol.tpa.storage.db.core.repository.projection.TravelPlanCoverageRow;

@Repository
@RequiredArgsConstructor
public class DefaultPlanCoverageRepository implements PlanCoverageRepository {

    private final JpaPlanCoverageRepository jpaRepository;

    @Override
    public List<TravelPlanCoverageRow> findRowsByPlanId(Long planId) {
        return jpaRepository.findRowsByPlanId(planId);
    }
}