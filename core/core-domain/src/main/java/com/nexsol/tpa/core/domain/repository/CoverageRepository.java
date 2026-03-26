package com.nexsol.tpa.core.domain.repository;

import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.coverage.Coverage;

public interface CoverageRepository {
    Optional<Coverage> findByInsurerIdAndCoverageCode(Long insurerId, String coverageCode);

    List<Coverage> findAllByInsurerId(Long insurerId);

    List<Coverage> findAllByIds(List<Long> ids);
}
