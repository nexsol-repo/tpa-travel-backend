package com.nexsol.tpa.storage.db.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_insure_refund")
public class TravelInsureRefundEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false, length = 30)
    private TravelPaymentMethod refundMethod;

    @Column(name = "bank_name", length = 30)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "depositor_name", length = 50)
    private String depositorName;

    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    @Column(name = "refunded_at", nullable = false)
    private LocalDateTime refundedAt;

    @Builder
    public TravelInsureRefundEntity(
            Long paymentId,
            Long contractId,
            BigDecimal refundAmount,
            TravelPaymentMethod refundMethod,
            String bankName,
            String accountNumber,
            String depositorName,
            String refundReason) {
        this.paymentId = paymentId;
        this.contractId = contractId;
        this.refundAmount = refundAmount;
        this.refundMethod = refundMethod;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.depositorName = depositorName;
        this.refundReason = refundReason;
        this.refundedAt = LocalDateTime.now();
    }
}
