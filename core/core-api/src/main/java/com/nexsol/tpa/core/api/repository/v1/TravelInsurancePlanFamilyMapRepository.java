package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurancePlanFamilyMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelInsurancePlanFamilyMapRepository extends JpaRepository<TravelInsurancePlanFamilyMapEntity, Long> {

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyIdIn(List<Long> familyIds);
}
