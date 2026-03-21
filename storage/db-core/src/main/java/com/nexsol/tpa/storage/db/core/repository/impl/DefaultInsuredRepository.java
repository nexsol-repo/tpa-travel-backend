package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.contract.InsuredPerson;
import com.nexsol.tpa.core.domain.repository.InsuredRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsuredRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultInsuredRepository implements InsuredRepository {

    private final JpaInsuredRepository jpaRepository;

    @Override
    public InsuredPerson save(InsuredPerson person) {
        TravelInsuredEntity entity = TravelInsuredEntity.fromDomain(person);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public List<InsuredPerson> findByContractId(Long contractId) {
        return jpaRepository.findByContractIdAndDeletedAtIsNullOrderByIdAsc(contractId).stream()
                .map(TravelInsuredEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<InsuredPerson> findContractorByContractId(Long contractId) {
        return Optional.ofNullable(
                        jpaRepository.findByContractIdAndIsContractorTrueAndDeletedAtIsNull(
                                contractId))
                .map(TravelInsuredEntity::toDomain);
    }

    @Override
    public List<InsuredPerson> findByContractIds(Collection<Long> contractIds) {
        return jpaRepository.findByContractIds(contractIds).stream()
                .map(TravelInsuredEntity::toDomain)
                .toList();
    }
}
