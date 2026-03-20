package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.SubscriptionApiResult;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.payment.PaymentReader;
import com.nexsol.tpa.core.domain.payment.PaymentWriter;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionWriter {

    private final ContractWriter contractWriter;
    private final PaymentReader paymentReader;
    private final PaymentWriter paymentWriter;

    public void updateSubscription(TravelContractEntity contract, SubscriptionApiResult apiResult) {
        contractWriter.updateWithMeritzResult(
                contract,
                apiResult.ttPrem(),
                apiResult.polNo(),
                apiResult.quotGrpNo(),
                apiResult.quotReqNo());
    }

    public void complete(TravelContractEntity contract) {
        contractWriter.markCompleted(contract);
    }

    public void createCompletedPayment(TravelContractEntity contract) {
        if (paymentReader.existsByContractId(contract.getId())) {
            throw new CoreApiException(
                    CoreApiErrorType.CONTRACT_ALREADY_PAID,
                    "payment already exists. contractId=" + contract.getId());
        }
        paymentWriter.createCompleted(contract.getId(), contract.getTotalPremium());
    }
}
