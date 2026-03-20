package com.nexsol.tpa.core.domain.certificate;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.core.domain.subscription.SubscriptionInsuredReader;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final MeritzContractClient meritzClient;
    private final ContractReader contractReader;
    private final SubscriptionInsuredReader subscriptionInsuredReader;
    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;
    private final CertificateLinkIssuer certificateLinkIssuer;

    /** 증명서 원본 응답 반환 */
    public MeritzBridgeApiResponse issue(String company, CertificateCommand cmd) {
        if (cmd == null || cmd.contractId() == null) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST, "contractId is required");
        }

        String otptDiv = normalizeOrDefault(cmd.otptDiv(), "A");
        String otptTpCd = normalizeOrDefault(cmd.otptTpCd(), "V");

        CertificateParams params = resolveParams(cmd.contractId());
        validateParams(params);

        MeritzBridgeApiResponse res =
                meritzClient.issueCertificateRaw(
                        company,
                        params.polNo,
                        params.pdCd,
                        params.quotGrpNo,
                        params.quotReqNo,
                        otptDiv,
                        otptTpCd);

        snapshotAppender.append(cmd.contractId(), params.insurerId, "CERTIFICATE", toJson(res));

        return res;
    }

    /** 증명서 링크 URL만 반환 */
    public String issueLink(String company, Long contractId, String otptDiv, String otptTpCd) {
        return certificateLinkIssuer.issue(company, contractId, otptDiv, otptTpCd);
    }

    private CertificateParams resolveParams(Long contractId) {
        TravelContractEntity contract = contractReader.getById(contractId);
        TravelInsurancePlanEntity plan = subscriptionInsuredReader.findRepPlan(contractId);

        return new CertificateParams(
                contract.getInsurerId(),
                contract.getPolicyNumber(),
                plan.getProductCode(),
                contract.getMeritzQuoteGroupNumber(),
                contract.getMeritzQuoteRequestNumber());
    }

    private void validateParams(CertificateParams params) {
        ContractValidator.requireNotBlank(params.polNo, "policyNumber(polNo) is required");
        ContractValidator.requireNotBlank(params.quotGrpNo, "quotGrpNo is required");
        ContractValidator.requireNotBlank(params.quotReqNo, "quotReqNo is required");
        ContractValidator.requireNotBlank(params.pdCd, "pdCd(productCode) is required");
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

    private record CertificateParams(
            Long insurerId, String polNo, String pdCd, String quotGrpNo, String quotReqNo) {}
}
