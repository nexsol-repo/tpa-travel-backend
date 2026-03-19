package com.nexsol.tpa.core.domain.cancel;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.contract.ContractValidator.CancelValidation;
import com.nexsol.tpa.core.domain.payment.PaymentReader;
import com.nexsol.tpa.core.domain.refund.RefundCommand;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurePaymentEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelService {

    private final MeritzContractClient meritzClient;
    private final ContractReader contractReader;
    private final ContractValidator contractValidator;
    private final PaymentReader paymentReader;
    private final CancelWriter cancelWriter;

    public Long cancel(String company, RefundCommand refundCommand) {
        Long contractId = refundCommand.contractId();
        TravelContractEntity contract = contractReader.getById(contractId);
        TravelInsurePaymentEntity payment = paymentReader.getByContractId(contract.getId());

        CancelValidation validation = contractValidator.validateCancel(payment);
        if (validation.isAlreadyCanceled()) {
            return contractId;
        }

        ContractValidator.requireNotBlank(
                contract.getPolicyNumber(), "policyNumber(polNo) is required");

        // 1. 외부 API 호출 (tx 밖)
        meritzClient.cancelContract(
                company,
                contract.getPolicyNumber(),
                contract.getMeritzQuoteGroupNumber(),
                contract.getMeritzQuoteRequestNumber());

        // 2. DB 일괄 업데이트 (tx 안 — CancelWriter)
        cancelWriter.save(contract, payment, refundCommand);

        return contractId;
    }
}