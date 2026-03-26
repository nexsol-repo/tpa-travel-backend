package com.nexsol.tpa.core.domain.certificate;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceContractClient;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.CertificateLinkResult;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.core.domain.subscription.SubscriptionInsuredReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateLinkIssuer {

    private final InsuranceContractClient meritzClient;
    private final ContractReader contractReader;
    private final SubscriptionInsuredReader subscriptionInsuredReader;
    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;

    public String issue(String company, Long contractId, String otptDiv, String otptTpCd) {
        String div = normalizeOrDefault(otptDiv, "A");
        String tp = normalizeOrDefault(otptTpCd, "V");

        ContractInfo contract = contractReader.getById(contractId);
        InsurancePlan plan = subscriptionInsuredReader.findRepPlan(contractId);

        String polNo = contract.policyNumber();
        String pdCd = plan.productCode();
        String quotGrpNo = contract.quote().groupNumber();
        String quotReqNo = contract.quote().requestNumber();

        ContractValidator.requireNotBlank(polNo, "policyNumber(polNo) is required");
        ContractValidator.requireNotBlank(quotGrpNo, "quotGrpNo is required");
        ContractValidator.requireNotBlank(quotReqNo, "quotReqNo is required");
        ContractValidator.requireNotBlank(pdCd, "pdCd(productCode) is required");

        CertificateLinkResult result =
                meritzClient.issueCertificate(company, polNo, pdCd, quotGrpNo, quotReqNo, div, tp);

        snapshotAppender.append(contractId, contract.insurerId(), "CERTIFICATE", toJson(result));

        return result.linkUrl();
    }

    private static String normalizeOrDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[CERTIFICATE] JSON 직렬화 실패", e);
            return "{}";
        }
    }
}
