package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurancePlanFamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelInsurancePlanFamilyRepository extends JpaRepository<TravelInsurancePlanFamilyEntity, Long> {

    List<TravelInsurancePlanFamilyEntity>
    findByInsurerIdAndInsuranceProductNameAndIsActiveTrueAndDeletedAtIsNullOrderBySortOrderAsc(
            Long insurerId, String insuranceProductName
    );
}
