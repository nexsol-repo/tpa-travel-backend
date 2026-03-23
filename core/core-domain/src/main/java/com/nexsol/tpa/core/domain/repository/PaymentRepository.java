package com.nexsol.tpa.core.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.refund.Refund;

public interface PaymentRepository {
    Payment save(Payment payment);

    void cancelPayment(Long paymentId, Refund refund);

    Optional<Payment> findByContractId(Long contractId);

    boolean existsByContractId(Long contractId);

    List<Payment> findByContractIdIn(Collection<Long> contractIds);
}
