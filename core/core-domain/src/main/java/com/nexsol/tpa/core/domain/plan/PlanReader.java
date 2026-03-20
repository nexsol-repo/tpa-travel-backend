package com.nexsol.tpa.core.domain.plan;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurancePlanRepository;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanReader {

    private final TravelInsurancePlanRepository planRepository;

    private final TravelInsurerRepository insurerRepository;

    public TravelInsurancePlanEntity getById(Long planId) {
        return planRepository
                .findById(planId)
                .orElseThrow(() -> new IllegalStateException("plan not found. planId=" + planId));
    }

    public TravelInsurerEntity getInsurerById(Long insurerId) {
        return insurerRepository
                .findById(insurerId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "insurer not found. insurerId=" + insurerId));
    }
}
