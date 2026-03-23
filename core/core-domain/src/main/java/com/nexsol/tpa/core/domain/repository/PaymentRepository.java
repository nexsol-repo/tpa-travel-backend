package com.nexsol.tpa.core.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.payment.Payment;

public interface PaymentRepository {
    Payment save(Payment payment);

    void markCanceled(Long paymentId);

    Optional<Payment> findByContractId(Long contractId);

    boolean existsByContractId(Long contractId);

    List<Payment> findByContractIdIn(Collection<Long> contractIds);
}
