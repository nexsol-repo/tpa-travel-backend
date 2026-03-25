package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.coverage.FamilyCoverage;
import com.nexsol.tpa.core.domain.repository.FamilyCoverageRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelFamilyCoverageEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaFamilyCoverageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultFamilyCoverageRepository implements FamilyCoverageRepository {

    private final JpaFamilyCoverageRepository jpaRepository;

    @Override
    public List<FamilyCoverage> findByFamilyId(Long familyId) {
        return jpaRepository.findByFamilyIdOrderBySortOrderAsc(familyId).stream()
                .map(TravelFamilyCoverageEntity::toDomain)
                .toList();
    }

    @Override
    public List<FamilyCoverage> findByFamilyIds(List<Long> familyIds) {
        return jpaRepository.findByFamilyIdInOrderByFamilyIdAscSortOrderAsc(familyIds).stream()
                .map(TravelFamilyCoverageEntity::toDomain)
                .toList();
    }
}
