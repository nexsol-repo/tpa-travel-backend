package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record PlanCoverage(
        Long planId,
        String coverageCode,
        String coverageName,
        String displayName,
        boolean included,
        int sortOrder,
        boolean majorCoverage,
        boolean titleYn,
        String categoryCode,
        String claimReasonOverride,
        String claimContentOverride,
        String subTitleOverride,
        String subContentOverride) {}