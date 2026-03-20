package com.nexsol.tpa.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record Payment(
        Long id,
        String paymentMethod,
        String status,
        BigDecimal paidAmount,
        LocalDateTime paymentDate,
        LocalDateTime cancelDate) {}
