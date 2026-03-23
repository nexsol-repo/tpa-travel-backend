package com.nexsol.tpa.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.refund.Refund;
import com.nexsol.tpa.core.domain.repository.PaymentRepository;
import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;

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

    public void cancelPayment(Payment payment, Refund refund) {
        paymentRepository.cancelPayment(payment.id(), refund);
    }
}
