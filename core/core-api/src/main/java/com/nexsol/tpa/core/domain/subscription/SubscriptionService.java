package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.contract.SubscriptionApiResult;
import com.nexsol.tpa.core.domain.certificate.CertificateLinkIssuer;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final ContractReader contractReader;
    private final ContractValidator contractValidator;
    private final SubscriptionValidator subscriptionValidator;
    private final SubscriptionEstimateSaver subscriptionEstimateSaver;
    private final SubscriptionWriter subscriptionWriter;
    private final SubscriptionSnapshotAppender subscriptionSnapshotAppender;
    private final SubscriptionAlimtalkAppender subscriptionAlimtalkAppender;
    private final SubscriptionResultReader subscriptionResultReader;
    private final CertificateLinkIssuer certificateLinkIssuer;

    @Transactional
    public SubscriptionResult subscribe(String company, SubscriptionCommand cmd) {
        TravelContractEntity contract = contractReader.getById(cmd.contractId());
        contractValidator.requirePending(contract);
        subscriptionValidator.validate(cmd, contract);

        SubscriptionApiResult apiResult =
                subscriptionEstimateSaver.save(company, contract, cmd);

        if (!apiResult.success()) {
            subscriptionSnapshotAppender.appendFail(contract, apiResult.rawData());
            return SubscriptionResult.fail(apiResult.errCd(), apiResult.errMsg());
        }

        subscriptionWriter.updateSubscription(contract, apiResult);
        issueCertificateLink(company, contract);
        subscriptionWriter.complete(contract);
        subscriptionWriter.createCompletedPayment(contract);
        subscriptionSnapshotAppender.appendSuccess(contract, apiResult.rawData());
        subscriptionAlimtalkAppender.appendCompleted(contract);
        return subscriptionResultReader.read(contract);
    }

    private void issueCertificateLink(String company, TravelContractEntity contract) {
        try {
            String policyLink = certificateLinkIssuer.issue(company, contract.getId(), "A", "V");
            contract.updatePolicyLink(policyLink);
        } catch (Exception e) {
            log.warn(
                    "joinCertificate failed. contractId={}, msg={}",
                    contract.getId(),
                    e.getMessage(),
                    e);
        }
    }
}