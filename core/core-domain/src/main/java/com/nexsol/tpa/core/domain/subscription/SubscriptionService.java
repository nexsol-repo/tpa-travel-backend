package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.certificate.CertificateLinkIssuer;
import com.nexsol.tpa.core.domain.client.SubscriptionProvider;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractUpdater;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.contract.ContractWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final ContractReader contractReader;
    private final ContractValidator contractValidator;
    private final ContractUpdater contractUpdater;
    private final ContractWriter contractWriter;
    private final SubscriptionValidator subscriptionValidator;
    private final SubscriptionProvider subscriptionProvider;
    private final SubscriptionWriter subscriptionWriter;
    private final SubscriptionSnapshotAppender subscriptionSnapshotAppender;
    private final SubscriptionResultReader subscriptionResultReader;
    private final SubscriptionAlimtalkAppender subscriptionAlimtalkAppender;
    private final CertificateLinkIssuer certificateLinkIssuer;

    public SubscriptionResult subscribe(String company, SubscriptionCommand cmd) {
        ContractInfo contract = contractReader.getById(cmd.contractId());
        contractValidator.requirePending(contract);
        subscriptionValidator.validate(cmd, contract);

        EstimateSaveResult apiResult = subscriptionProvider.estimateSave(company, contract, cmd);

        if (!apiResult.success()) {
            subscriptionSnapshotAppender.appendFail(contract, apiResult.rawData());
            return SubscriptionResult.fail(apiResult.errCd(), apiResult.errMsg());
        }

        contract = subscriptionWriter.updateSubscription(contract, apiResult);
        contract = issueCertificateLink(company, contract);
        contract = subscriptionWriter.complete(contract);
        subscriptionWriter.createCompletedPayment(contract);
        subscriptionSnapshotAppender.appendSuccess(contract, apiResult.rawData());
        subscriptionAlimtalkAppender.appendCompleted(contract.id());
        return subscriptionResultReader.read(contract);
    }

    private ContractInfo issueCertificateLink(String company, ContractInfo contract) {
        try {
            String policyLink = certificateLinkIssuer.issue(company, contract.id(), "A", "V");
            ContractInfo updated = contractUpdater.updatePolicyLink(contract, policyLink);
            contractWriter.writerContract(updated);
            return updated;
        } catch (Exception e) {
            log.warn(
                    "joinCertificate failed. contractId={}, msg={}",
                    contract.id(),
                    e.getMessage(),
                    e);
            return contract;
        }
    }
}
