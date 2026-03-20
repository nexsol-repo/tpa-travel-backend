package com.nexsol.tpa.core.domain.coverage;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsuranceCoverageRepository;
import com.nexsol.tpa.core.error.CoreException;
import com.nexsol.tpa.core.error.CoreErrorType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CoverageReader {

    private final InsuranceCoverageRepository coverageRepository;

    public InsuranceCoverage getByInsurerIdAndCoverageCode(
            Long insurerId, String coverageCode) {
        return coverageRepository
                .findByInsurerIdAndCoverageCode(insurerId, coverageCode)
                .orElseThrow(
                        () -> new CoreException(
                                CoreErrorType.NOT_FOUND_DATA,
                                "coverage not found: " + coverageCode));
    }
}