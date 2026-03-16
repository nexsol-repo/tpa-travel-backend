package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanFamilyMapEntity;

public interface TravelInsurancePlanFamilyMapRepository
        extends JpaRepository<TravelInsurancePlanFamilyMapEntity, Long> {

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyIdIn(List<Long> familyIds);

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyIdIn(Collection<Long> familyIds);

    List<TravelInsurancePlanFamilyMapEntity> findByFamilyId(Long familyId);
}
