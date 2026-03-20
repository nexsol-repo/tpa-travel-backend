package com.nexsol.tpa.core.domain.coverage;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoverageService {

    private final CoverageReader coverageReader;

    public CoverageResult getCoverage(Long insurerId, String coverageCode) {
        InsuranceCoverage coverage =
                coverageReader.getByInsurerIdAndCoverageCode(insurerId, coverageCode);

        return new CoverageResult(
                coverage.id(),
                coverage.coverageCode(),
                coverage.coverageName(),
                coverage.groupCode(),
                coverage.claimReason(),
                coverage.claimContent(),
                coverage.subTitle(),
                coverage.subContent());
    }
}