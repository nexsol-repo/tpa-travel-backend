package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractUpdater;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.payment.PaymentReader;
import com.nexsol.tpa.core.domain.payment.PaymentWriter;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionWriter {

    private final ContractUpdater contractUpdater;
    private final ContractWriter contractWriter;
    private final PaymentReader paymentReader;
    private final PaymentWriter paymentWriter;

    public ContractInfo updateSubscription(ContractInfo contract, EstimateSaveResult apiResult) {
        ContractInfo updated =
                contractUpdater.updateMeritzResult(
                        contract,
                        apiResult.ttPrem(),
                        apiResult.polNo(),
                        apiResult.quotGrpNo(),
                        apiResult.quotReqNo());
        contractWriter.writerContract(updated);
        return updated;
    }

    public ContractInfo complete(ContractInfo contract) {
        ContractInfo completed = contractUpdater.markCompleted(contract);
        contractWriter.writerContract(completed);
        return completed;
    }

    public void createCompletedPayment(ContractInfo contract) {
        if (paymentReader.existsByContractId(contract.id())) {
            throw new CoreException(
                    CoreErrorType.CONTRACT_ALREADY_PAID,
                    "payment already exists. contractId=" + contract.id());
        }
        paymentWriter.createCompleted(contract.id(), contract.totalPremium());
    }
}
