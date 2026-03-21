package com.nexsol.tpa.core.domain.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;

import lombok.Builder;

@Builder
public record Refund(
        Long id,
        Long paymentId,
        Long contractId,
        BigDecimal refundAmount,
        TravelPaymentMethod refundMethod,
        String bankName,
        String accountNumber,
        String depositorName,
        String refundReason,
        LocalDateTime refundedAt) {}
