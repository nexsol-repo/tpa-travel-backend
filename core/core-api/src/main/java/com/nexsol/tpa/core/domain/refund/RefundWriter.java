package com.nexsol.tpa.core.domain.refund;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelRefundEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsureRefundRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefundWriter {

    private final TravelInsureRefundRepository refundRepository;

    public TravelRefundEntity create(RefundCommand command) {
        return refundRepository.save(
                TravelRefundEntity.builder()
                        .paymentId(command.paymentId())
                        .contractId(command.contractId())
                        .refundAmount(command.refundAmount())
                        .refundMethod(command.refundMethod())
                        .bankName(command.bankName())
                        .accountNumber(command.accountNumber())
                        .depositorName(command.depositorName())
                        .refundReason(command.refundReason())
                        .build());
    }
}
