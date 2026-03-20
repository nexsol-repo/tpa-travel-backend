package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsurancePlanRepository;
import com.nexsol.tpa.storage.db.core.repository.projection.PlanFamilyPlanRow;

@Repository
@RequiredArgsConstructor
public class DefaultInsurancePlanRepository implements InsurancePlanRepository {

    private final JpaInsurancePlanRepository jpaRepository;

    @Override
    public Optional<TravelInsurancePlanEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<TravelInsurancePlanEntity> findActiveByInsurerId(Long insurerId) {
        return jpaRepository.findByInsurerIdAndIsActiveTrueOrderBySortOrderAsc(insurerId);
    }

    @Override
    public List<TravelInsurancePlanEntity> findByIdIn(Collection<Long> ids) {
        return jpaRepository.findByIdIn(ids);
    }

    @Override
    public List<TravelInsurancePlanEntity> findByIdInAndIsActiveTrue(Collection<Long> ids) {
        return jpaRepository.findByIdInAndIsActiveTrue(ids);
    }

    @Override
    public List<TravelInsurancePlanEntity> findByFamilyIdAndIsActiveTrue(Long familyId) {
        return jpaRepository.findByFamilyIdAndIsActiveTrue(familyId);
    }

    @Override
    public List<PlanFamilyPlanRow> findActiveFamilyPlans(Long insurerId) {
        return jpaRepository.findActiveFamilyPlans(insurerId);
    }
}