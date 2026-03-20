package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanFamilyEntity;

public interface JpaInsurancePlanFamilyRepository
        extends JpaRepository<TravelInsurancePlanFamilyEntity, Long> {

    List<TravelInsurancePlanFamilyEntity>
            findByInsurerIdAndInsuranceProductNameAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(
                    Long insurerId, String insuranceProductName);

    List<TravelInsurancePlanFamilyEntity>
            findByInsurerIdAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(Long insurerId);
}
