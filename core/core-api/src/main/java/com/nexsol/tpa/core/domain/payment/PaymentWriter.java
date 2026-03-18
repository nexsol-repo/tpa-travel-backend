package com.nexsol.tpa.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurePaymentEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurePaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentWriter {

    private final TravelInsurePaymentRepository paymentRepository;

    public TravelInsurePaymentEntity createCompleted(Long contractId, BigDecimal amount) {
        return paymentRepository.save(
                TravelInsurePaymentEntity.builder()
                        .contractId(contractId)
                        .paymentMethod(TravelPaymentMethod.CARD)
                        .paidAmount(amount)
                        .status(TravelPaymentStatus.COMPLETED)
                        .paymentDate(LocalDateTime.now())
                        .build());
    }

    public void markCanceled(TravelInsurePaymentEntity payment) {
        payment.markCanceled(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}
