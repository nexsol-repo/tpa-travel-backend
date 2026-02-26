package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsuranceCoverageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelInsuranceCoverageRepository extends JpaRepository<TravelInsuranceCoverageEntity, Long> {

    Optional<TravelInsuranceCoverageEntity> findByInsurerIdAndCoverageCodeAndDeletedAtIsNull(Long insurerId,
            String coverageCode);

}