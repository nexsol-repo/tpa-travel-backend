package com.nexsol.tpa.core.domain.plan;

import java.util.*;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.core.domain.repository.PlanFamilyRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanReader {

    private final InsurancePlanRepository planRepository;
    private final PlanFamilyRepository familyRepository;

    public InsurancePlan getById(Long planId) {
        return planRepository
                .findById(planId)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "plan not found. planId=" + planId));
    }

    public List<InsurancePlan> findActiveByInsurerId(Long insurerId) {
        return planRepository.findActiveByInsurerId(insurerId);
    }

    public List<InsurancePlan> findByIdIn(Collection<Long> ids) {
        return planRepository.findByIdIn(ids);
    }

    public List<InsurancePlan> findByFamilyIdAndIsActiveTrue(Long familyId) {
        return planRepository.findByFamilyIdAndIsActiveTrue(familyId);
    }

    public List<PlanFamily> loadAllFamilies(Long insurerId) {
        List<PlanFamily> families = familyRepository.findActiveByInsurerId(insurerId);
        if (families.isEmpty()) return List.of();

        List<InsurancePlan> plans = planRepository.findActiveByInsurerId(insurerId);
        if (plans.isEmpty()) return List.of();

        Map<Long, List<InsurancePlan>> grouped = new LinkedHashMap<>();
        for (InsurancePlan p : plans) {
            if (p.familyId() != null) {
                grouped.computeIfAbsent(p.familyId(), k -> new ArrayList<>()).add(p);
            }
        }

        return families.stream()
                .map(
                        f -> {
                            List<InsurancePlan> familyPlans =
                                    grouped.getOrDefault(f.familyId(), List.of());
                            if (familyPlans.isEmpty()) return null;
                            return PlanFamily.builder()
                                    .familyId(f.familyId())
                                    .familyName(f.familyName())
                                    .isLoss(f.isLoss())
                                    .repPlan(pickRepPlan(familyPlans))
                                    .plans(familyPlans)
                                    .build();
                        })
                .filter(Objects::nonNull)
                .toList();
    }

    private InsurancePlan pickRepPlan(List<InsurancePlan> plans) {
        return plans.stream()
                .filter(p -> Objects.equals(p.ageGroupId(), 2))
                .findFirst()
                .orElse(plans.getFirst());
    }
}
