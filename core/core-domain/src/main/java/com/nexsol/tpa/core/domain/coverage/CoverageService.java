package com.nexsol.tpa.core.domain.coverage;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuranceCoverageEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoverageService {

    private final CoverageReader coverageReader;

    public CoverageResult getCoverage(Long insurerId, String coverageCode) {
        TravelInsuranceCoverageEntity entity =
                coverageReader.getByInsurerIdAndCoverageCode(insurerId, coverageCode);

        return new CoverageResult(
                entity.getId(),
                entity.getCoverageCode(),
                entity.getCoverageName(),
                entity.getGroupCode(),
                entity.getClaimReason(),
                entity.getClaimContent(),
                entity.getSubTitle(),
                entity.getSubContent());
    }
}
