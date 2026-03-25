package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelFamilyCoverageEntity;

public interface JpaFamilyCoverageRepository
        extends JpaRepository<TravelFamilyCoverageEntity, Long> {

    List<TravelFamilyCoverageEntity> findByFamilyIdOrderBySortOrderAsc(Long familyId);

    List<TravelFamilyCoverageEntity> findByFamilyIdInOrderByFamilyIdAscSortOrderAsc(
            List<Long> familyIds);
}
