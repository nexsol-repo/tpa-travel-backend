package com.nexsol.tpa.core.domain.plan;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.core.domain.repository.InsurerRepository;
import com.nexsol.tpa.core.error.CoreException;
import com.nexsol.tpa.core.error.CoreErrorType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanReader {

    private final InsurancePlanRepository planRepository;

    private final InsurerRepository insurerRepository;

    public InsurancePlan getById(Long planId) {
        return planRepository
                .findById(planId)
                .orElseThrow(() -> new CoreException(
                        CoreErrorType.NOT_FOUND_DATA,
                        "plan not found. planId=" + planId));
    }

    public Insurer getInsurerById(Long insurerId) {
        return insurerRepository
                .findById(insurerId)
                .orElseThrow(
                        () -> new CoreException(
                                CoreErrorType.NOT_FOUND_DATA,
                                "insurer not found. insurerId=" + insurerId));
    }
}