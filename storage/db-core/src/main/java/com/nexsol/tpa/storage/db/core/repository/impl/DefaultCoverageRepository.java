package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.coverage.Coverage;
import com.nexsol.tpa.core.domain.repository.CoverageRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelCoverageEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaCoverageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultCoverageRepository implements CoverageRepository {

    private final JpaCoverageRepository jpaRepository;

    @Override
    public Optional<Coverage> findByInsurerIdAndCoverageCode(Long insurerId, String coverageCode) {
        return jpaRepository
                .findByInsurerIdAndCoverageCodeAndIsActiveTrue(insurerId, coverageCode)
                .map(TravelCoverageEntity::toDomain);
    }

    @Override
    public List<Coverage> findAllByInsurerId(Long insurerId) {
        return jpaRepository.findByInsurerIdAndIsActiveTrueOrderByIdAsc(insurerId).stream()
                .map(TravelCoverageEntity::toDomain)
                .toList();
    }

    @Override
    public List<Coverage> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream().map(TravelCoverageEntity::toDomain).toList();
    }
}
