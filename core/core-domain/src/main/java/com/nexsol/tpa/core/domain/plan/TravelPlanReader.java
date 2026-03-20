package com.nexsol.tpa.core.domain.plan;

import java.util.*;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.core.domain.repository.PlanCoverageRepository;
import com.nexsol.tpa.core.domain.repository.projection.PlanFamilyPlanRow;
import com.nexsol.tpa.core.domain.repository.projection.TravelPlanCoverageRow;

import lombok.RequiredArgsConstructor;

/**
 * 플랜/패밀리/담보 DB 순수 조회 도구 (Tool Layer).
 * 비즈니스 필터링 없이 데이터만 로딩한다.
 */
@Component
@RequiredArgsConstructor
public class TravelPlanReader {

    private final InsurancePlanRepository planRepository;
    private final PlanCoverageRepository coverageRepository;

    public record PlanFamily(
            Long familyId,
            String familyName,
            boolean isLoss,
            InsurancePlan repPlan,
            List<InsurancePlan> plans) {}

    /**
     * 보험사의 전체 활성 패밀리 + 플랜을 조회한다.
     * 비즈니스 필터링(planType, 실손제외)은 하지 않는다.
     */
    public List<PlanFamily> loadAllFamilies(Long insurerId) {
        List<PlanFamilyPlanRow> rows = planRepository.findActiveFamilyPlans(insurerId);
        if (rows.isEmpty()) return List.of();

        Map<Long, InsurancePlan> planById = loadPlanEntities(rows);
        if (planById.isEmpty()) return List.of();

        Map<Long, List<InsurancePlan>> familyPlans = new LinkedHashMap<>();
        Map<Long, String> familyNames = new LinkedHashMap<>();
        Map<Long, Boolean> familyLossFlags = new LinkedHashMap<>();

        for (PlanFamilyPlanRow r : rows) {
            InsurancePlan p = planById.get(r.getPlanId());
            if (p == null) continue;
            familyPlans.computeIfAbsent(r.getFamilyId(), k -> new ArrayList<>()).add(p);
            familyNames.putIfAbsent(r.getFamilyId(), r.getFamilyName());
            familyLossFlags.putIfAbsent(r.getFamilyId(), r.getIsLoss() != null && r.getIsLoss());
        }

        List<PlanFamily> result = new ArrayList<>();
        for (var e : familyPlans.entrySet()) {
            String name = familyNames.get(e.getKey());
            if (name == null) continue;

            List<InsurancePlan> plans = e.getValue();
            InsurancePlan repPlan = pickRepPlan(plans);
            boolean isLoss = familyLossFlags.getOrDefault(e.getKey(), false);
            result.add(new PlanFamily(e.getKey(), name, isLoss, repPlan, plans));
        }
        return result;
    }

    /**
     * 플랜의 DB 담보 목록을 조회한다.
     */
    public List<TravelPlanCoverageRow> loadCoverages(Long planId) {
        return coverageRepository.findRowsByPlanId(planId);
    }

    // ── internal ──

    private Map<Long, InsurancePlan> loadPlanEntities(List<PlanFamilyPlanRow> rows) {
        Set<Long> planIds = new LinkedHashSet<>();
        for (PlanFamilyPlanRow r : rows) {
            planIds.add(r.getPlanId());
        }
        Map<Long, InsurancePlan> map = new HashMap<>();
        for (InsurancePlan p : planRepository.findByIdIn(planIds)) {
            map.put(p.id(), p);
        }
        return map;
    }

    private InsurancePlan pickRepPlan(List<InsurancePlan> plans) {
        return plans.stream()
                .filter(p -> Objects.equals(p.ageGroupId(), 2))
                .findFirst()
                .orElse(plans.getFirst());
    }
}