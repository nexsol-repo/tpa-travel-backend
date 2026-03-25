package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record FamilyCoverage(
        Long id,
        Long familyId,
        Long coverageId,
        int sortOrder,
        boolean featured,
        String displayName) {}
