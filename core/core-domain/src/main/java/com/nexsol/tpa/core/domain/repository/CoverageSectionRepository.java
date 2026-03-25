package com.nexsol.tpa.core.domain.repository;

import java.util.List;

import com.nexsol.tpa.core.domain.coverage.CoverageSection;

public interface CoverageSectionRepository {
    List<CoverageSection> findAllActive();
}
