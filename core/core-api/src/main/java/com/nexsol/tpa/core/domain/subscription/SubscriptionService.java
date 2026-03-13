package com.nexsol.tpa.core.domain.subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient.EstimateSaveRequest;
import com.nexsol.tpa.client.meritz.contract.SubscriptionApiResult;
import com.nexsol.tpa.core.domain.certificate.CertificateService;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MeritzContractClient meritzClient;
    private final ContractReader contractReader;
    private final ContractValidator contractValidator;
    private final PlanReader planReader;
    private final CertificateService certificateService;
    private final SubscriptionValidator subscriptionValidator;
    private final SubscriptionInsuredReader subscriptionInsuredReader;
    private final SubscriptionWriter subscriptionWriter;
    private final SubscriptionSnapshotAppender subscriptionSnapshotAppender;
    private final SubscriptionAlimtalkAppender subscriptionAlimtalkAppender;
    private final SubscriptionResultReader subscriptionResultReader;

    @Transactional
    public SubscriptionResult subscribe(String company, SubscriptionCommand cmd) {
        TravelContractEntity contract = contractReader.getById(cmd.contractId());
        contractValidator.requirePending(contract);
        subscriptionValidator.validate(cmd, contract);

        TravelInsurancePlanEntity plan = planReader.getById(contract.getPlanId());
        TravelInsurerEntity insurer = planReader.getInsurerById(plan.getInsurerId());

        SubscriptionApiResult apiResult = callEstimateSave(company, contract, plan, cmd);

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
        return subscriptionResultReader.read(contract, plan, insurer);
    }

    private SubscriptionApiResult callEstimateSave(
            String company,
            TravelContractEntity contract,
            TravelInsurancePlanEntity plan,
            SubscriptionCommand cmd) {

        String sbcpDt = LocalDate.now().format(YYYYMMDD);

        List<EstimateSaveRequest.InsuredPerson> insuredPeople =
                subscriptionInsuredReader.findEstimateSaveInsuredPeople(contract.getId(), plan);

        log.info(
                "[SUBSCRIPTION] insureStartDate={}, insureEndDate={}",
                contract.getInsureStartDate(),
                contract.getInsureEndDate());

        return meritzClient.estimateSave(
                new EstimateSaveRequest(
                        company,
                        contract.getPolicyNumber(),
                        plan.getProductCode(),
                        plan.getUnitProductCode(),
                        sbcpDt,
                        contract.getInsureStartDate().format(YYYYMMDD),
                        contract.getInsureEndDate().format(YYYYMMDD),
                        contract.getCountryCode(),
                        cmd.cardNo(),
                        cmd.efctPrd(),
                        cmd.dporNm(),
                        cmd.dporCd(),
                        insuredPeople));
    }

    private void issueCertificateLink(String company, TravelContractEntity contract) {
        try {
            String policyLink = certificateService.issueLink(company, contract.getId(), "A", "V");
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
