package com.nexsol.tpa.core.domain.plan;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanReader {

    private final InsurancePlanRepository planRepository;

    public InsurancePlan readById(Long planId) {
        if (planId == null) return null;
        return planRepository.findById(planId).orElse(null);
    }

    public InsurancePlan getById(Long planId) {
        return planRepository
                .findById(planId)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "plan not found. planId=" + planId));
    }
}
