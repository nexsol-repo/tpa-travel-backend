package com.nexsol.tpa.client.meritz.payment;

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
public class MeritzPaymentClient {

    private final MeritzBridgeFeignClient bridgeClient;
    private final CompaniesConfigsProperties companies;

    public MeritzBridgeApiResponse approveCard(
            String company,
            String polNo,
            String estNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String apvAmt) {

        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("company", cfg.getCompanyCode());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", polNo);
        body.put("estNo", estNo);
        body.put("crdNo", crdNo);
        body.put("efctPrd", efctPrd);
        body.put("dporNm", dporNm);
        body.put("dporCd", dporCd);
        body.put("apvAmt", apvAmt);

        MeritzBridgeApiResponse res = bridgeClient.cardApprove(body);
        log.info("[PAYMENT][CARD_APPROVE] success={}, errCd={}", res.isSuccess(), res.getErrCd());
        return res;
    }

    public MeritzBridgeApiResponse cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt) {

        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("company", cfg.getCompanyCode());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", polNo);
        body.put("estNo", estNo);
        body.put("orgApvNo", orgApvNo);
        body.put("cncAmt", cncAmt);

        MeritzBridgeApiResponse res = bridgeClient.cardCancel(body);
        log.info("[PAYMENT][CARD_CANCEL] success={}, errCd={}", res.isSuccess(), res.getErrCd());
        return res;
    }
}
