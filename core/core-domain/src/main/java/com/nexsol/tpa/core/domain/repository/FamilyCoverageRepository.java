package com.nexsol.tpa.core.domain.repository;

import java.util.List;

import com.nexsol.tpa.core.domain.coverage.FamilyCoverage;

public interface FamilyCoverageRepository {
    List<FamilyCoverage> findByFamilyId(Long familyId);

    List<FamilyCoverage> findByFamilyIds(List<Long> familyIds);
}
