package com.nexsol.tpa.core.domain.coverage;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuranceCoverageEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsuranceCoverageRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CoverageReader {

    private final TravelInsuranceCoverageRepository coverageRepository;

    public TravelInsuranceCoverageEntity getByInsurerIdAndCoverageCode(
            Long insurerId, String coverageCode) {
        return coverageRepository
                .findByInsurerIdAndCoverageCodeAndDeletedAtIsNull(insurerId, coverageCode)
                .orElseThrow(
                        () -> new IllegalArgumentException("coverage not found: " + coverageCode));
    }
}
