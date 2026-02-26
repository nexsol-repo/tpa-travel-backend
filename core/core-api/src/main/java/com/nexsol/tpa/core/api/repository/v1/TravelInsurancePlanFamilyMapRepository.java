package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurancePlanFamilyEntity;
import com.nexsol.tpa.core.api.entity.TravelInsurancePlanFamilyMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TravelInsurancePlanFamilyMapRepository
        extends JpaRepository<TravelInsurancePlanFamilyMapEntity, Long> {

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyIdIn(List<Long> familyIds);

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyIdIn(Collection<Long> familyIds);

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyId(Long familyId);

}
