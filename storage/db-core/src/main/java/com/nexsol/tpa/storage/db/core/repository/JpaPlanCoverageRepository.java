package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelPlanCoverageEntity;

public interface JpaPlanCoverageRepository extends JpaRepository<TravelPlanCoverageEntity, Long> {

    List<TravelPlanCoverageEntity> findByPlanIdOrderBySortOrderAscIdAsc(Long planId);
}
