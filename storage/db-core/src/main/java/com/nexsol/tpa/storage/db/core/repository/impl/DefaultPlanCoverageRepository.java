package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.coverage.PlanCoverage;
import com.nexsol.tpa.core.domain.repository.PlanCoverageRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuranceCoverageEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelPlanCoverageEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsuranceCoverageRepository;
import com.nexsol.tpa.storage.db.core.repository.JpaPlanCoverageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultPlanCoverageRepository implements PlanCoverageRepository {

    private final JpaPlanCoverageRepository jpaPlanCoverageRepository;
    private final JpaInsuranceCoverageRepository jpaInsuranceCoverageRepository;

    @Override
    public List<PlanCoverage> findByPlanId(Long planId) {
        List<TravelPlanCoverageEntity> planCoverages =
                jpaPlanCoverageRepository.findByPlanIdOrderBySortOrderAscIdAsc(planId);
        if (planCoverages.isEmpty()) {
            return List.of();
        }

        Set<Long> coverageIds =
                planCoverages.stream()
                        .map(TravelPlanCoverageEntity::getCoverageId)
                        .collect(Collectors.toSet());

        Map<Long, TravelInsuranceCoverageEntity> coverageMap =
                jpaInsuranceCoverageRepository.findAllById(coverageIds).stream()
                        .collect(
                                Collectors.toMap(
                                        TravelInsuranceCoverageEntity::getId, Function.identity()));

        return planCoverages.stream()
                .map(
                        pc -> {
                            TravelInsuranceCoverageEntity cov = coverageMap.get(pc.getCoverageId());
                            String coverageCode = cov != null ? cov.getCoverageCode() : null;
                            String coverageName = cov != null ? cov.getCoverageName() : null;
                            return pc.toDomain(coverageCode, coverageName);
                        })
                .toList();
    }
}
