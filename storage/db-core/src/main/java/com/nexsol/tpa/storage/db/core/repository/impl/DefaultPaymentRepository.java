package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.PaymentRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelPaymentEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaPaymentRepository;

@Repository
@RequiredArgsConstructor
public class DefaultPaymentRepository implements PaymentRepository {

    private final JpaPaymentRepository jpaRepository;

    @Override
    public TravelPaymentEntity save(TravelPaymentEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TravelPaymentEntity> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId);
    }

    @Override
    public boolean existsByContractId(Long contractId) {
        return jpaRepository.existsByContractId(contractId);
    }

    @Override
    public List<TravelPaymentEntity> findByContractIdIn(Collection<Long> contractIds) {
        return jpaRepository.findByContractIdIn(contractIds);
    }
}