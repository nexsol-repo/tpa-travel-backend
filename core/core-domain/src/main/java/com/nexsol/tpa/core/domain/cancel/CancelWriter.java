package com.nexsol.tpa.core.domain.cancel;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.payment.PaymentWriter;
import com.nexsol.tpa.core.domain.refund.ContractRefund;
import com.nexsol.tpa.core.domain.refund.RefundWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.core.enums.TravelPaymentMethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelWriter {

    private final PaymentWriter paymentWriter;
    private final RefundWriter refundWriter;
    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;

    public void save(ContractInfo contract, Payment payment, ContractRefund contractRefund) {

        paymentWriter.markCanceled(payment);

        refundWriter.create(
                new ContractRefund(
                        contract.id(),
                        payment.id(),
                        contract.totalPremium(),
                        TravelPaymentMethod.CARD,
                        contractRefund.bankName(),
                        contractRefund.accountNumber(),
                        contractRefund.depositorName(),
                        contractRefund.refundReason()));

        snapshotAppender.append(contract.id(), contract.insurerId(), "CANCEL", toJson("CANCELED"));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[CANCEL] JSON 직렬화 실패", e);
            return "{}";
        }
    }
}
