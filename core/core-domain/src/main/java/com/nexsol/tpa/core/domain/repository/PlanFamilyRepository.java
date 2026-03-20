package com.nexsol.tpa.core.domain.repository;

import java.util.List;

import com.nexsol.tpa.core.domain.plan.PlanFamily;

public interface PlanFamilyRepository {
    List<PlanFamily> findActiveByInsurerId(Long insurerId);
}