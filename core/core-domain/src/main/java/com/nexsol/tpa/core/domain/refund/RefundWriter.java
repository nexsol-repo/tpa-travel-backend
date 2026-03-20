package com.nexsol.tpa.core.domain.refund;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.RefundRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefundWriter {

    private final RefundRepository refundRepository;

    public Refund create(ContractRefund command) {
        return refundRepository.save(
                Refund.builder()
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