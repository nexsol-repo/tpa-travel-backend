package com.nexsol.tpa.storage.db.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuranceCoverageEntity;

public interface TravelInsuranceCoverageRepository
        extends JpaRepository<TravelInsuranceCoverageEntity, Long> {

    Optional<TravelInsuranceCoverageEntity> findByInsurerIdAndCoverageCodeAndDeletedAtIsNull(
            Long insurerId, String coverageCode);
}
