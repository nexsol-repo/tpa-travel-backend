package com.nexsol.tpa.provider.meritz.certificate;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.certificate.CertificateLink;
import com.nexsol.tpa.core.domain.client.CertificateProvider;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzCertificateProvider implements CertificateProvider {

    private final InsuranceContractClient contractClient;
    private final ObjectMapper objectMapper;

    @Override
    public CertificateLink issueCertificate(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd) {

        BridgeApiResult res =
                contractClient.issueCertificateRaw(
                        company, polNo, pdCd, quotGrpNo, quotReqNo, otptDiv, otptTpCd);

        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "증명서 발급 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }

        JsonNode data = parseData(res.data());
        return new CertificateLink(data.path("rltLinkUrl").asText(null));
    }

    private JsonNode parseData(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("[MERITZ_CERTIFICATE] 응답 파싱 실패", e);
            throw new CoreException(CoreErrorType.DEFAULT_ERROR, "증명서 응답 파싱 실패");
        }
    }
}
