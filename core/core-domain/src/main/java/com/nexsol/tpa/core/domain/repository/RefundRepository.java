package com.nexsol.tpa.core.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.refund.Refund;

public interface RefundRepository {
    Refund save(Refund refund);

    Optional<Refund> findByPaymentId(Long paymentId);

    Optional<Refund> findByContractId(Long contractId);

    List<Refund> findByContractIdIn(Collection<Long> contractIds);
}
