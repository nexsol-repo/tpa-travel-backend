package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.InsuranceCoverageRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuranceCoverageEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsuranceCoverageRepository;

@Repository
@RequiredArgsConstructor
public class DefaultInsuranceCoverageRepository implements InsuranceCoverageRepository {

    private final JpaInsuranceCoverageRepository jpaRepository;

    @Override
    public Optional<TravelInsuranceCoverageEntity> findByInsurerIdAndCoverageCode(
            Long insurerId, String coverageCode) {
        return jpaRepository.findByInsurerIdAndCoverageCodeAndDeletedAtIsNull(
                insurerId, coverageCode);
    }
}