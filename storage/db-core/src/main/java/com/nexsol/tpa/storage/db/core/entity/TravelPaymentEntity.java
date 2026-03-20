package com.nexsol.tpa.storage.db.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_payment")
public class TravelPaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false, unique = true)
    private Long contractId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private TravelPaymentMethod paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TravelPaymentStatus status;

    @Builder
    public TravelPaymentEntity(
            Long contractId,
            TravelPaymentMethod paymentMethod,
            LocalDateTime paymentDate,
            BigDecimal paidAmount,
            TravelPaymentStatus status) {
        this.contractId = contractId;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        this.status = status == null ? TravelPaymentStatus.COMPLETED : status;
    }

    public static TravelPaymentEntity createReady(
            Long contractId, TravelPaymentMethod method, BigDecimal amount) {
        return TravelPaymentEntity.builder()
                .contractId(contractId)
                .paymentMethod(method)
                .paidAmount(amount)
                .status(TravelPaymentStatus.COMPLETED)
                .build();
    }

    public void markPaid(LocalDateTime paidAt) {
        this.status = TravelPaymentStatus.COMPLETED;
        this.paymentDate = paidAt == null ? LocalDateTime.now() : paidAt;
    }

    public void markCanceled(LocalDateTime canceledAt) {
        this.status = TravelPaymentStatus.CANCELED;
        this.cancelDate = canceledAt == null ? LocalDateTime.now() : canceledAt;
    }
}
