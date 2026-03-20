package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record InsuranceCoverage(
        Long id,
        Long insurerId,
        String coverageCode,
        String coverageName,
        String groupCode,
        String claimReason,
        String claimContent,
        String subTitle,
        String subContent) {}