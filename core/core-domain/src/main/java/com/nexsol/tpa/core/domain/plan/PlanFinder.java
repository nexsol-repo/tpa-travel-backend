package com.nexsol.tpa.core.domain.plan;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.core.domain.repository.PlanFamilyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanFinder {

    private final InsurancePlanRepository planRepository;
    private final PlanFamilyRepository familyRepository;

    public Map<Long, InsurancePlan> findMapByIds(List<Long> planIds) {
        return planRepository.findByIdIn(planIds).stream()
                .collect(Collectors.toMap(InsurancePlan::id, p -> p));
    }

    public List<InsurancePlan> findByIdIn(Collection<Long> ids) {
        return planRepository.findByIdIn(ids);
    }

    public List<InsurancePlan> findActiveByInsurerId(Long insurerId) {
        return planRepository.findActiveByInsurerId(insurerId);
    }

    public List<InsurancePlan> findByFamilyIdAndIsActiveTrue(Long familyId) {
        return planRepository.findByFamilyIdAndIsActiveTrue(familyId);
    }

    public List<PlanFamily> findAllFamilies(Long insurerId) {
        List<PlanFamily> families = familyRepository.findActiveByInsurerId(insurerId);
        if (families.isEmpty()) return List.of();

        List<InsurancePlan> plans = findActiveByInsurerId(insurerId);
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
