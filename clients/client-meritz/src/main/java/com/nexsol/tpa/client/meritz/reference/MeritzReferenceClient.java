package com.nexsol.tpa.client.meritz.reference;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeFeignClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties.CompanyConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzReferenceClient {

    private final MeritzBridgeFeignClient bridgeClient;
    private final CompaniesConfigsProperties companies;

    public MeritzBridgeApiResponse getPlans(String stdDt) {
        CompanyConfig cfg = companies.getTpa();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("company", cfg.getCompanyCode());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", "15540-19125");
        body.put("stdDt", stdDt);

        MeritzBridgeApiResponse res = bridgeClient.planInquiry(body);
        log.info("[REFERENCE][PLAN_INQUIRY] success={}, errCd={}", res.isSuccess(), res.getErrCd());
        return res;
    }

    public MeritzBridgeApiResponse getCityNationCodes(String keyword, String type) {
        CompanyConfig cfg = companies.getTpa();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("company", cfg.getCompanyCode());
        body.put("srchVal", keyword);
        body.put("srchCnd", type);
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());

        MeritzBridgeApiResponse res = bridgeClient.cityCountryCode(body);
        log.info("[REFERENCE][CITY_NATION] success={}, errCd={}", res.isSuccess(), res.getErrCd());
        return res;
    }
}
