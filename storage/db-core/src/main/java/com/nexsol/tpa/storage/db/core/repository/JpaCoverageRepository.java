package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelCoverageEntity;

public interface JpaCoverageRepository extends JpaRepository<TravelCoverageEntity, Long> {

    Optional<TravelCoverageEntity> findByInsurerIdAndCoverageCodeAndIsActiveTrue(
            Long insurerId, String coverageCode);

    List<TravelCoverageEntity> findByInsurerIdAndIsActiveTrueOrderByIdAsc(Long insurerId);
}
