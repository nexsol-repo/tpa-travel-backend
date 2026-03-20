package com.nexsol.tpa.core.domain.plan;

import lombok.Builder;

@Builder
public record InsurancePlan(
        Long id,
        String insuranceProductName,
        String planName,
        String planFullName,
        String productCode,
        String unitProductCode,
        String planGroupCode,
        String planCode,
        Integer ageGroupId) {}
