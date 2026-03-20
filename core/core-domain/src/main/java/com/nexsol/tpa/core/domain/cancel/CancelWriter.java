package com.nexsol.tpa.core.domain.cancel;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nexsol.tpa.core.domain.payment.PaymentWriter;
import com.nexsol.tpa.core.domain.refund.ContractRefund;
import com.nexsol.tpa.core.domain.refund.RefundWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelPaymentEntity;

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

    @Transactional
    public void save(
            TravelContractEntity contract,
            TravelPaymentEntity payment,
            ContractRefund contractRefund) {

        paymentWriter.markCanceled(payment);

        refundWriter.create(
                new ContractRefund(
                        contract.getId(),
                        payment.getId(),
                        contract.getTotalPremium(),
                        TravelPaymentMethod.CARD,
                        contractRefund.bankName(),
                        contractRefund.accountNumber(),
                        contractRefund.depositorName(),
                        contractRefund.refundReason()));

        snapshotAppender.append(
                contract.getId(), contract.getInsurerId(), "CANCEL", toJson("CANCELED"));
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
