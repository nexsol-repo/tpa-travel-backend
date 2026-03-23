package com.nexsol.tpa.client.meritz.payment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeFeignClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties.CompanyConfig;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.domain.client.InsurancePaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzPaymentClient implements InsurancePaymentClient {

    private final MeritzBridgeFeignClient bridgeClient;
    private final CompaniesConfigsProperties companies;

    @Override
    public BridgeApiResult approveCard(
            String company,
            String polNo,
            String quotGrpNo,
            String quotReqNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String rcptPrem) {

        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("company", cfg.getCompanyCode());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", polNo);
        body.put("rcptPrem", rcptPrem);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("crdNo", crdNo);
        card.put("efctPrd", efctPrd);
        card.put("dporNm", dporNm);
        card.put("dporCd", dporCd);
        body.put("ctrTrsInfBcVo", card);

        Map<String, Object> quot = new LinkedHashMap<>();
        quot.put("quotGrpNo", quotGrpNo);
        quot.put("quotReqNo", quotReqNo);
        quot.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("coprGrupTrvCtrQuotInfCbcVo", List.of(quot));

        MeritzBridgeApiResponse res = bridgeClient.cardApprove(body);
        log.info("[PAYMENT][CARD_APPROVE] success={}, errCd={}", res.isSuccess(), res.getErrCd());
        return new BridgeApiResult(res.isSuccess(), res.getErrCd(), res.getErrMsg(), res.getData());
    }

    @Override
    public BridgeApiResult cancelCard(
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
        return new BridgeApiResult(res.isSuccess(), res.getErrCd(), res.getErrMsg(), res.getData());
    }
}
