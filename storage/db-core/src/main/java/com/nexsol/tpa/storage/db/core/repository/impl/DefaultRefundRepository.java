package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.refund.Refund;
import com.nexsol.tpa.core.domain.repository.RefundRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelRefundEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaRefundRepository;

@Repository
@RequiredArgsConstructor
public class DefaultRefundRepository implements RefundRepository {

    private final JpaRefundRepository jpaRepository;

    @Override
    public Refund save(Refund refund) {
        TravelRefundEntity entity = TravelRefundEntity.fromDomain(refund);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Refund> findByPaymentId(Long paymentId) {
        return jpaRepository.findByPaymentId(paymentId).map(TravelRefundEntity::toDomain);
    }

    @Override
    public Optional<Refund> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId).map(TravelRefundEntity::toDomain);
    }

    @Override
    public List<Refund> findByContractIdIn(Collection<Long> contractIds) {
        return jpaRepository.findByContractIdIn(contractIds)
                .stream().map(TravelRefundEntity::toDomain).toList();
    }
}