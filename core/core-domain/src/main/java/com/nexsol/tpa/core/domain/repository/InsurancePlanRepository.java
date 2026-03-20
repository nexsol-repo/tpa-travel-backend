package com.nexsol.tpa.core.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.repository.projection.PlanFamilyPlanRow;

public interface InsurancePlanRepository {
    Optional<InsurancePlan> findById(Long id);
    List<InsurancePlan> findActiveByInsurerId(Long insurerId);
    List<InsurancePlan> findByIdIn(Collection<Long> ids);
    List<InsurancePlan> findByIdInAndIsActiveTrue(Collection<Long> ids);
    List<InsurancePlan> findByFamilyIdAndIsActiveTrue(Long familyId);
    List<PlanFamilyPlanRow> findActiveFamilyPlans(Long insurerId);
}