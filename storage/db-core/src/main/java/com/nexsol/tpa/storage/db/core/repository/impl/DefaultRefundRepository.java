package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.RefundRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelRefundEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaRefundRepository;

@Repository
@RequiredArgsConstructor
public class DefaultRefundRepository implements RefundRepository {

    private final JpaRefundRepository jpaRepository;

    @Override
    public TravelRefundEntity save(TravelRefundEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TravelRefundEntity> findByPaymentId(Long paymentId) {
        return jpaRepository.findByPaymentId(paymentId);
    }

    @Override
    public Optional<TravelRefundEntity> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId);
    }

    @Override
    public List<TravelRefundEntity> findByContractIdIn(Collection<Long> contractIds) {
        return jpaRepository.findByContractIdIn(contractIds);
    }
}