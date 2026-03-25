package com.nexsol.tpa.core.domain.cancel;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.CancelProvider;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.payment.PaymentReader;
import com.nexsol.tpa.core.domain.refund.ContractRefund;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelService {

    private final CancelProvider cancelProvider;
    private final ContractReader contractReader;
    private final ContractValidator contractValidator;
    private final PaymentReader paymentReader;
    private final CancelWriter cancelWriter;

    public Long cancel(String company, ContractRefund contractRefund) {
        Long contractId = contractRefund.contractId();
        ContractInfo contract = contractReader.getById(contractId);
        Payment payment = paymentReader.getByContractId(contract.id());

        contractValidator.requireCancelable(payment);

        if (!contractValidator.isAlreadyCanceled(payment)) {
            ContractValidator.requireNotBlank(
                    contract.policyNumber(), "policyNumber(polNo) is required");

            cancelProvider.cancelContract(
                    company,
                    contract.policyNumber(),
                    contract.quote().groupNumber(),
                    contract.quote().requestNumber());

            cancelWriter.save(contract, payment, contractRefund);
        }

        return contractId;
    }
}
