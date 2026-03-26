package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.refund.Refund;

import lombok.Builder;

@Builder
public record RefundDetail(
        BigDecimal refundAmount,
        String refundMethod,
        String bankName,
        String accountNumber,
        String depositorName,
        String refundReason,
        LocalDateTime refundedAt) {

    public static RefundDetail from(Refund refund) {
        return RefundDetail.builder()
                .refundAmount(refund.refundAmount())
                .refundMethod(refund.refundMethod() != null ? refund.refundMethod().name() : null)
                .bankName(refund.bankName())
                .accountNumber(refund.accountNumber())
                .depositorName(refund.depositorName())
                .refundReason(refund.refundReason())
                .refundedAt(refund.refundedAt())
                .build();
    }
}
