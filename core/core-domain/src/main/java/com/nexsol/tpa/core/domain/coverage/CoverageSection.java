package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record CoverageSection(
        Long id,
        String sectionCode,
        String sectionName,
        String description,
        int sortOrder,
        boolean active) {}
