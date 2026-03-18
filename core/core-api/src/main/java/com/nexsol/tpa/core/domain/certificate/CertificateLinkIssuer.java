package com.nexsol.tpa.core.domain.certificate;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.CertificateApiResult;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateLinkIssuer {

    private final MeritzContractClient meritzClient;
    private final ContractReader contractReader;
    private final PlanReader planReader;
    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;

    public String issue(String company, Long contractId, String otptDiv, String otptTpCd) {
        String div = normalizeOrDefault(otptDiv, "A");
        String tp = normalizeOrDefault(otptTpCd, "V");

        TravelContractEntity contract = contractReader.getById(contractId);
        TravelInsurancePlanEntity plan = planReader.getById(contract.getPlanId());

        String polNo = contract.getPolicyNumber();
        String pdCd = plan.getProductCode();
        String quotGrpNo = contract.getMeritzQuoteGroupNumber();
        String quotReqNo = contract.getMeritzQuoteRequestNumber();

        ContractValidator.requireNotBlank(polNo, "policyNumber(polNo) is required");
        ContractValidator.requireNotBlank(quotGrpNo, "quotGrpNo is required");
        ContractValidator.requireNotBlank(quotReqNo, "quotReqNo is required");
        ContractValidator.requireNotBlank(pdCd, "pdCd(productCode) is required");

        CertificateApiResult result =
                meritzClient.issueCertificate(company, polNo, pdCd, quotGrpNo, quotReqNo, div, tp);

        snapshotAppender.append(contractId, contract.getInsurerId(), "CERTIFICATE", toJson(result));

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
