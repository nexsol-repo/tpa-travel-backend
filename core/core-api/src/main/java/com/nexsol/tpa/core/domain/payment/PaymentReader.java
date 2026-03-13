package com.nexsol.tpa.core.domain.payment;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurePaymentEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurePaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentReader {

    private final TravelInsurePaymentRepository paymentRepository;

    public TravelInsurePaymentEntity getByContractId(Long contractId) {
        return paymentRepository
                .findByContractId(contractId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "payment not found. contractId=" + contractId));
    }

    public boolean existsByContractId(Long contractId) {
        return paymentRepository.existsByContractId(contractId);
    }
}
