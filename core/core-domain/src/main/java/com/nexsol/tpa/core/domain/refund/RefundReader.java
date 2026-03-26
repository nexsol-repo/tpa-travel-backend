package com.nexsol.tpa.core.domain.refund;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.RefundRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefundReader {

    private final RefundRepository refundRepository;

    public Refund getByContractId(Long contractId) {
        return refundRepository
                .findByContractId(contractId)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "refund not found. contractId=" + contractId));
    }

    public Refund readByContractId(Long contractId) {
        return refundRepository.findByContractId(contractId).orElse(null);
    }

    public Refund getByPaymentId(Long paymentId) {
        return refundRepository
                .findByPaymentId(paymentId)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "refund not found. paymentId=" + paymentId));
    }
}
