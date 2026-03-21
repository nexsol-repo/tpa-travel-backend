package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.repository.PaymentRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelPaymentEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaPaymentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultPaymentRepository implements PaymentRepository {

    private final JpaPaymentRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        TravelPaymentEntity entity = TravelPaymentEntity.fromDomain(payment);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Payment> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId).map(TravelPaymentEntity::toDomain);
    }

    @Override
    public boolean existsByContractId(Long contractId) {
        return jpaRepository.existsByContractId(contractId);
    }

    @Override
    public List<Payment> findByContractIdIn(Collection<Long> contractIds) {
        return jpaRepository.findByContractIdIn(contractIds).stream()
                .map(TravelPaymentEntity::toDomain)
                .toList();
    }
}
