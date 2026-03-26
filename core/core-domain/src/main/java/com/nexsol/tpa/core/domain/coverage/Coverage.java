package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record Coverage(
        Long id,
        Long insurerId,
        String coverageCode,
        String coverageName,
        String sectionCode,
        String claimReason,
        String claimContent,
        String subTitle,
        String subContent) {}
