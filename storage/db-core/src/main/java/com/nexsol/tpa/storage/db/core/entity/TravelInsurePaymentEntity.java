package com.nexsol.tpa.storage.db.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "travel_insure_payment")
public class TravelInsurePaymentEntity extends BaseEntity {

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

    @Builder.Default
    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TravelPaymentStatus status = TravelPaymentStatus.COMPLETED;

    public static TravelInsurePaymentEntity createReady(
            Long contractId, TravelPaymentMethod method, BigDecimal amount) {
        TravelInsurePaymentEntity p = new TravelInsurePaymentEntity();
        p.contractId = contractId;
        p.paymentMethod = method;
        p.paidAmount = amount == null ? BigDecimal.ZERO : amount;
        p.status = TravelPaymentStatus.COMPLETED;
        return p;
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
