package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelCoverageSectionEntity;

public interface JpaCoverageSectionRepository
        extends JpaRepository<TravelCoverageSectionEntity, Long> {

    List<TravelCoverageSectionEntity> findByIsActiveTrueOrderBySortOrderAsc();
}
