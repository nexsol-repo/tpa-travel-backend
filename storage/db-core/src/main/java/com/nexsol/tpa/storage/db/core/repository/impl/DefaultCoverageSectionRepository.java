package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.coverage.CoverageSection;
import com.nexsol.tpa.core.domain.repository.CoverageSectionRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelCoverageSectionEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaCoverageSectionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultCoverageSectionRepository implements CoverageSectionRepository {

    private final JpaCoverageSectionRepository jpaRepository;

    @Override
    public List<CoverageSection> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(TravelCoverageSectionEntity::toDomain)
                .toList();
    }
}
