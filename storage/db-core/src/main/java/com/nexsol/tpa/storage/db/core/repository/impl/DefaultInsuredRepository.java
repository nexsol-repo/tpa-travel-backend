package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.InsuredRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsuredRepository;

@Repository
@RequiredArgsConstructor
public class DefaultInsuredRepository implements InsuredRepository {

    private final JpaInsuredRepository jpaRepository;

    @Override
    public TravelInsuredEntity save(TravelInsuredEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public List<TravelInsuredEntity> findByContractId(Long contractId) {
        return jpaRepository.findByContractIdAndDeletedAtIsNullOrderByIdAsc(contractId);
    }

    @Override
    public TravelInsuredEntity findContractorByContractId(Long contractId) {
        return jpaRepository.findByContractIdAndIsContractorTrueAndDeletedAtIsNull(contractId);
    }

    @Override
    public List<TravelInsuredEntity> findByContractIds(Collection<Long> contractIds) {
        return jpaRepository.findByContractIds(contractIds);
    }
}