package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

public interface JpaInsurancePlanRepository extends JpaRepository<TravelInsurancePlanEntity, Long> {

    List<TravelInsurancePlanEntity> findByInsurerIdAndIsActiveTrueOrderBySortOrderAsc(
            Long insurerId);

    List<TravelInsurancePlanEntity> findByIdIn(Collection<Long> ids);

    List<TravelInsurancePlanEntity> findByIdInAndIsActiveTrue(Collection<Long> ids);

    List<TravelInsurancePlanEntity> findByFamilyIdAndIsActiveTrue(Long familyId);
}
