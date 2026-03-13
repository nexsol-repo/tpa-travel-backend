package com.nexsol.tpa.core.domain.plan;

import java.util.*;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurancePlanRepository;
import com.nexsol.tpa.storage.db.core.repository.TravelPlanCoverageRepository;
import com.nexsol.tpa.storage.db.core.repository.projection.PlanFamilyPlanRow;
import com.nexsol.tpa.storage.db.core.repository.projection.TravelPlanCoverageRow;

import lombok.RequiredArgsConstructor;

/**
 * 플랜/패밀리/담보 DB 순수 조회 도구 (Tool Layer).
 * 비즈니스 필터링 없이 데이터만 로딩한다.
 */
@Component
@RequiredArgsConstructor
public class TravelPlanReader {

    private final TravelInsurancePlanRepository planRepository;
    private final TravelPlanCoverageRepository coverageRepository;

    public record PlanFamily(
            Long familyId,
            String familyName,
            TravelInsurancePlanEntity repPlan,
            List<TravelInsurancePlanEntity> plans) {}

    /**
     * 보험사의 전체 활성 패밀리 + 플랜을 조회한다.
     * 비즈니스 필터링(planType, 실손제외)은 하지 않는다.
     */
    public List<PlanFamily> loadAllFamilies(Long insurerId) {
        List<PlanFamilyPlanRow> rows = planRepository.findActiveFamilyPlans(insurerId);
        if (rows.isEmpty()) return List.of();

        Map<Long, TravelInsurancePlanEntity> planById = loadPlanEntities(rows);
        if (planById.isEmpty()) return List.of();

        Map<Long, List<TravelInsurancePlanEntity>> familyPlans = new LinkedHashMap<>();
        Map<Long, String> familyNames = new LinkedHashMap<>();

        for (PlanFamilyPlanRow r : rows) {
            TravelInsurancePlanEntity p = planById.get(r.getPlanId());
            if (p == null) continue;
            familyPlans.computeIfAbsent(r.getFamilyId(), k -> new ArrayList<>()).add(p);
            familyNames.putIfAbsent(r.getFamilyId(), r.getFamilyName());
        }

        List<PlanFamily> result = new ArrayList<>();
        for (var e : familyPlans.entrySet()) {
            String name = familyNames.get(e.getKey());
            if (name == null) continue;

            List<TravelInsurancePlanEntity> plans = e.getValue();
            TravelInsurancePlanEntity repPlan = pickRepPlan(plans);
            result.add(new PlanFamily(e.getKey(), name, repPlan, plans));
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

    private Map<Long, TravelInsurancePlanEntity> loadPlanEntities(List<PlanFamilyPlanRow> rows) {
        Set<Long> planIds = new LinkedHashSet<>();
        for (PlanFamilyPlanRow r : rows) {
            planIds.add(r.getPlanId());
        }
        Map<Long, TravelInsurancePlanEntity> map = new HashMap<>();
        for (TravelInsurancePlanEntity p : planRepository.findAllById(planIds)) {
            map.put(p.getId(), p);
        }
        return map;
    }

    private TravelInsurancePlanEntity pickRepPlan(List<TravelInsurancePlanEntity> plans) {
        return plans.stream()
                .filter(p -> Objects.equals(p.getAgeGroupId(), 2))
                .findFirst()
                .orElse(plans.getFirst());
    }
}
