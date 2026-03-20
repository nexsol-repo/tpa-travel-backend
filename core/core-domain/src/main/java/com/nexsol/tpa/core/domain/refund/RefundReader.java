package com.nexsol.tpa.core.domain.refund;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelRefundEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsureRefundRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefundReader {

    private final TravelInsureRefundRepository refundRepository;

    public TravelRefundEntity getByContractId(Long contractId) {
        return refundRepository
                .findByContractId(contractId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "refund not found. contractId=" + contractId));
    }

    public TravelRefundEntity getByPaymentId(Long paymentId) {
        return refundRepository
                .findByPaymentId(paymentId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "refund not found. paymentId=" + paymentId));
    }
}
