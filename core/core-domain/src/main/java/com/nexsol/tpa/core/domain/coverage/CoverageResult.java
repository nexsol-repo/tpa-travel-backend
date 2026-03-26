package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record CoverageResult(
        Long id,
        String coverageCode,
        String coverageName,
        String sectionCode,
        String sectionName,
        String claimReason,
        String claimContent,
        String subTitle,
        String subContent) {}
