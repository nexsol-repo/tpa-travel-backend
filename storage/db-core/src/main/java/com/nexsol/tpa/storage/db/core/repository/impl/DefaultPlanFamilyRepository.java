package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.repository.PlanFamilyRepository;
import com.nexsol.tpa.storage.db.core.repository.JpaInsurancePlanFamilyRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultPlanFamilyRepository implements PlanFamilyRepository {

    private final JpaInsurancePlanFamilyRepository jpaRepository;

    @Override
    public List<PlanFamily> findActiveByInsurerId(Long insurerId) {
        return jpaRepository
                .findByInsurerIdAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(insurerId)
                .stream()
                .map(e -> e.toDomain(List.of()))
                .toList();
    }
}
