package com.nexsol.tpa.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;
import com.nexsol.tpa.core.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentWriter {

    private final PaymentRepository paymentRepository;

    public Payment createCompleted(Long contractId, BigDecimal amount) {
        return paymentRepository.save(
                Payment.builder()
                        .contractId(contractId)
                        .paymentMethod(TravelPaymentMethod.CARD.name())
                        .paidAmount(amount)
                        .status(TravelPaymentStatus.COMPLETED.name())
                        .paymentDate(LocalDateTime.now())
                        .build());
    }

    public void markCanceled(Payment payment) {
        paymentRepository.save(
                Payment.builder()
                        .id(payment.id())
                        .contractId(payment.contractId())
                        .paymentMethod(payment.paymentMethod())
                        .paidAmount(payment.paidAmount())
                        .status(TravelPaymentStatus.CANCELED.name())
                        .paymentDate(payment.paymentDate())
                        .cancelDate(LocalDateTime.now())
                        .build());
    }
}