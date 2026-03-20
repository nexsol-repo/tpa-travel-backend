package com.nexsol.tpa.core.domain.plan;

import java.util.List;

import lombok.Builder;

@Builder
public record PlanFamily(
        Long familyId,
        String familyName,
        boolean isLoss,
        InsurancePlan repPlan,
        List<InsurancePlan> plans) {}