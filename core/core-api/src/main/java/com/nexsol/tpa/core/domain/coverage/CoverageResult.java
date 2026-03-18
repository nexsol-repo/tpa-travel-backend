package com.nexsol.tpa.core.domain.coverage;

public record CoverageResult(
        Long id,
        String coverageCode,
        String coverageName,
        String groupCode,
        String claimReason,
        String claimContent,
        String subTitle,
        String subContent) {}
