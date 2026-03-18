package com.nexsol.tpa.client.meritz.quote;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeFeignClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties.CompanyConfig;
import com.nexsol.tpa.client.meritz.dto.quote.MeritzHndyPremCmptBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzQuoteClient {

    private final MeritzBridgeFeignClient bridgeClient;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    /**
     * 보험료 산출 API 호출. 성공 시 raw JSON 문자열 반환, 실패 시 null 반환.
     */
    public String calculatePremium(PremiumRequest request) {
        CompanyConfig cfg = companies.resolve(request.company());

        MeritzHndyPremCmptBody body =
                new MeritzHndyPremCmptBody(
                        cfg.getCompanyCode(),
                        cfg.getGnrAflcoCd(),
                        cfg.getAflcoDivCd(),
                        cfg.getBizpeNo(),
                        cfg.getPolNo(),
                        request.productCode(),
                        request.unitProductCode(),
                        request.sbcpDt(),
                        request.insBgnDt(),
                        request.insEdDt(),
                        request.trvArCd(),
                        request.insuredList().size(),
                        request.insuredList().stream()
                                .map(
                                        i ->
                                                new MeritzHndyPremCmptBody.Insured(
                                                        i.planGroupCode(),
                                                        i.planCode(),
                                                        i.birth(),
                                                        i.gender(),
                                                        i.name(),
                                                        i.nameEng()))
                                .toList());

        MeritzBridgeApiResponse res = bridgeClient.premiumCalculate(body);
        if (!res.isSuccess()) {
            log.warn(
                    "[QUOTE] bridge API failed. errCd={}, errMsg={}",
                    res.getErrCd(),
                    res.getErrMsg());
            return null;
        }
        if (res.getData() == null) {
            log.warn("[QUOTE] bridge API returned null data");
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(res.getData());
            return json;
        } catch (Exception e) {
            log.error("[QUOTE] 응답 파싱 실패", e);
            return null;
        }
    }

    public record PremiumRequest(
            String company,
            String productCode,
            String unitProductCode,
            String sbcpDt,
            String insBgnDt,
            String insEdDt,
            String trvArCd,
            List<InsuredPerson> insuredList) {

        public record InsuredPerson(
                String planGroupCode,
                String planCode,
                String birth,
                String gender,
                String name,
                String nameEng) {}
    }
}
