package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsurancePlanRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultInsurancePlanRepository implements InsurancePlanRepository {

    private final JpaInsurancePlanRepository jpaRepository;

    @Override
    public Optional<InsurancePlan> findById(Long id) {
        return jpaRepository.findById(id).map(TravelInsurancePlanEntity::toDomain);
    }

    @Override
    public List<InsurancePlan> findActiveByInsurerId(Long insurerId) {
        return jpaRepository.findByInsurerIdAndIsActiveTrueOrderBySortOrderAsc(insurerId).stream()
                .map(TravelInsurancePlanEntity::toDomain)
                .toList();
    }

    @Override
    public List<InsurancePlan> findByIdIn(Collection<Long> ids) {
        return jpaRepository.findByIdIn(ids).stream()
                .map(TravelInsurancePlanEntity::toDomain)
                .toList();
    }

    @Override
    public List<InsurancePlan> findByIdInAndIsActiveTrue(Collection<Long> ids) {
        return jpaRepository.findByIdInAndIsActiveTrue(ids).stream()
                .map(TravelInsurancePlanEntity::toDomain)
                .toList();
    }

    @Override
    public List<InsurancePlan> findByFamilyIdAndIsActiveTrue(Long familyId) {
        return jpaRepository.findByFamilyIdAndIsActiveTrue(familyId).stream()
                .map(TravelInsurancePlanEntity::toDomain)
                .toList();
    }
}
