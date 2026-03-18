package com.nexsol.tpa.core.domain.refund;

import java.math.BigDecimal;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;

public record RefundCommand(
        Long contractId,
        Long paymentId,
        BigDecimal refundAmount,
        TravelPaymentMethod refundMethod,
        String bankName,
        String accountNumber,
        String depositorName,
        String refundReason) {}
