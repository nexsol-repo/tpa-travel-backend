package com.nexsol.tpa.core.domain.repository;

import java.util.Optional;

import com.nexsol.tpa.core.domain.coverage.InsuranceCoverage;

public interface InsuranceCoverageRepository {
    Optional<InsuranceCoverage> findByInsurerIdAndCoverageCode(Long insurerId, String coverageCode);
}