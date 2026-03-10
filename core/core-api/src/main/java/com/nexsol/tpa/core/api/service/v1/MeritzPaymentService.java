package com.nexsol.tpa.core.api.service.v1;

import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeRequest;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeResponse;
import com.nexsol.tpa.core.api.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.core.api.meritz.dto.MeritzApiRequest;
import com.nexsol.tpa.core.api.meritz.dto.payment.MeritzCardApproveBody;
import com.nexsol.tpa.core.api.meritz.dto.payment.MeritzCardCancelBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeritzPaymentService {

    private static final String CARD_APPROVE = "/b2b/v1/organ/meritz/handleOpapiTrvCtrCrdApv";

    private static final String CARD_CANCEL = "/b2b/v1/organ/meritz/handleOpapiTrvCtrCrdCnc";

    private final MeritzBridgeClient bridgeClient;

    private final CompaniesConfigsProperties companies;

    private final ObjectMapper objectMapper;

    public String approveCard(String companyCode, MeritzCardApproveBody body) {
        return call(companyCode, CARD_APPROVE, body, "[MERITZ][CARD_APPROVE]");
    }

    public String cancelCard(String companyCode, MeritzCardCancelBody body) {
        return call(companyCode, CARD_CANCEL, body, "[MERITZ][CARD_CANCEL]");
    }

    private String call(String companyCode, String endpoint, Object body, String logPrefix) {
        var cfg = resolve(companyCode);

        var wrapper = new MeritzApiRequest<>(new MeritzApiRequest.Header(null, null), body);

        logJson(logPrefix + "[REQ]", endpoint, wrapper);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), endpoint, "POST", headers(), wrapper));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    endpoint + " failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        log.info("{}[RES] endpoint={}, status={}", logPrefix, endpoint, res.getStatus());
        return res.getBody();
    }

    private Map<String, String> headers() {
        return Map.of("Content-Type", "application/json; charset=UTF-8");
    }

    private void logJson(String prefix, String endpoint, Object body) {
        try {
            log.info("{} endpoint={}, body={}", prefix, endpoint, objectMapper.writeValueAsString(body));
        }
        catch (Exception e) {
            log.info("{} endpoint={}, body=(json serialize fail) {}", prefix, endpoint, body);
        }
    }

    private CompaniesConfigsProperties.CompanyConfig resolve(String companyCode) {
        if (companyCode == null || companyCode.isBlank())
            companyCode = "TPA";
        if ("TPA".equalsIgnoreCase(companyCode))
            return companies.getTpa();
        if ("INSBOON".equalsIgnoreCase(companyCode))
            return companies.getInsboon();
        throw new IllegalArgumentException("Unknown companyCode: " + companyCode);
    }

}
