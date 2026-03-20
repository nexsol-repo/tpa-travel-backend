package com.nexsol.tpa.core.domain.payment;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.PaymentRepository;
import com.nexsol.tpa.core.error.CoreException;
import com.nexsol.tpa.core.error.CoreErrorType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentReader {

    private final PaymentRepository paymentRepository;

    public Payment getByContractId(Long contractId) {
        return paymentRepository
                .findByContractId(contractId)
                .orElseThrow(
                        () -> new CoreException(
                                CoreErrorType.NOT_FOUND_DATA,
                                "payment not found. contractId=" + contractId));
    }

    public boolean existsByContractId(Long contractId) {
        return paymentRepository.existsByContractId(contractId);
    }
}