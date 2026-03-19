package com.nexsol.tpa.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.storage.db.core.entity.TravelPaymentEntity;

import lombok.Builder;

@Builder
public record Payment(
        Long id,
        String paymentMethod,
        String status,
        BigDecimal paidAmount,
        LocalDateTime paymentDate,
        LocalDateTime cancelDate) {

    public static Payment of(TravelPaymentEntity e) {
        if (e == null) return null;
        return Payment.builder()
                .id(e.getId())
                .paymentMethod(e.getPaymentMethod() != null ? e.getPaymentMethod().name() : null)
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .paidAmount(e.getPaidAmount())
                .paymentDate(e.getPaymentDate())
                .cancelDate(e.getCancelDate())
                .build();
    }
}
