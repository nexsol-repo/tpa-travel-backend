package com.nexsol.tpa.client.meritz.contract;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeFeignClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties.CompanyConfig;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzContractClient implements InsuranceContractClient {

    private final MeritzBridgeFeignClient bridgeClient;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    // ── estimateSave (가입확정) ──

    @Override
    public SubscriptionResult estimateSave(EstimateSaveCommand cmd) {
        CompanyConfig cfg = companies.resolve(cmd.company());

        Map<String, Object> body = companyBody(cfg);
        body.put("polNo", cmd.polNo());
        body.put("pdCd", cmd.pdCd());
        body.put("untPdCd", cmd.untPdCd());
        body.put("sbcpDt", cmd.sbcpDt());
        body.put("insBgnDt", cmd.insBgnDt());
        body.put("insEdDt", cmd.insEdDt());
        body.put("trvArCd", cmd.trvArCd());
        body.put("inspeCnt", cmd.insuredPeople().size());

        List<Map<String, Object>> insuredVos =
                cmd.insuredPeople().stream()
                        .map(
                                p -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("inspeRsidNo", null);
                                    m.put("inspeBdt", p.inspeBdt());
                                    m.put("gndrCd", p.gndrCd());
                                    m.put("inspeNm", p.inspeNm());
                                    m.put("engInspeNm", p.engInspeNm());
                                    m.put("planGrpCd", p.planGrpCd());
                                    m.put("planCd", p.planCd());
                                    return m;
                                })
                        .toList();
        body.put("opapiTrvPremCmptInspeInfCbcVo", insuredVos);

        if (cmd.grupSalChnDivCd() != null && !cmd.grupSalChnDivCd().isBlank()) {
            body.put("grupSalChnDivCd", cmd.grupSalChnDivCd());
        }

        if (cmd.cardNo() != null && !cmd.cardNo().isBlank()) {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("crdNo", cmd.cardNo());
            card.put("efctPrd", cmd.efctPrd());
            card.put("dporNm", cmd.dporNm());
            card.put("dporCd", cmd.dporCd());
            body.put("ctrTrsInfBcVo", card);
        }

        MeritzBridgeApiResponse res = bridgeClient.estimateSave(body);
        log.info("[MERITZ][EST_SAVE] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            return SubscriptionResult.fail(res.getErrCd(), res.getErrMsg(), res);
        }

        JsonNode data = parseData(res.getData());
        return SubscriptionResult.success(
                parseBigDecimal(data, "ttPrem"),
                data.path("polNo").asText(null),
                data.path("quotGrpNo").asText(null),
                data.path("quotReqNo").asText(null),
                res);
    }

    // ── 계약 취소 ──

    @Override
    public void cancelContract(String company, String polNo, String quotGrpNo, String quotReqNo) {
        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = companyBody(cfg);
        body.put("polNo", polNo);
        if (quotGrpNo != null && !quotGrpNo.isBlank()) {
            body.put("quotGrpNo", quotGrpNo);
        }
        if (quotReqNo != null && !quotReqNo.isBlank()) {
            body.put("quotReqNo", quotReqNo);
        }

        MeritzBridgeApiResponse res = bridgeClient.contractCancel(body);
        log.info("[MERITZ][CANCEL] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            throw new MeritzContractClientException(
                    "계약취소 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
    }

    // ── 증명서 ──

    @Override
    public CertificateLinkResult issueCertificate(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd) {

        MeritzBridgeApiResponse res =
                callCertificate(company, polNo, pdCd, quotGrpNo, quotReqNo, otptDiv, otptTpCd);

        JsonNode data = parseData(res.getData());
        return new CertificateLinkResult(data.path("rltLinkUrl").asText(null));
    }

    @Override
    public BridgeApiResult issueCertificateRaw(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd) {

        MeritzBridgeApiResponse res =
                callCertificate(company, polNo, pdCd, quotGrpNo, quotReqNo, otptDiv, otptTpCd);
        return new BridgeApiResult(res.isSuccess(), res.getErrCd(), res.getErrMsg(), res.getData());
    }

    private MeritzBridgeApiResponse callCertificate(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd) {

        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = companyBody(cfg);
        body.put("polNo", polNo);
        body.put("pdCd", pdCd);
        body.put("quotGrpNo", quotGrpNo);
        body.put("quotReqNo", quotReqNo);
        body.put("otptDiv", otptDiv);
        body.put("otptTpCd", otptTpCd);

        MeritzBridgeApiResponse res = bridgeClient.certificate(body);
        log.info("[MERITZ][CERTIFICATE] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            throw new MeritzContractClientException(
                    "가입증명서 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
        return res;
    }

    // ── 계약 목록/상세 조회 ──

    @Override
    public BridgeApiResult contractList(String company, Map<String, Object> bodyFields) {
        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = companyBody(cfg);
        body.putAll(bodyFields);

        MeritzBridgeApiResponse res = bridgeClient.contractList(body);
        log.info("[MERITZ][CONTRACT_LIST] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            throw new MeritzContractClientException(
                    "계약목록조회 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
        return new BridgeApiResult(res.isSuccess(), res.getErrCd(), res.getErrMsg(), res.getData());
    }

    @Override
    public BridgeApiResult contractDetail(String company, Map<String, Object> bodyFields) {
        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = companyBody(cfg);
        body.putAll(bodyFields);

        MeritzBridgeApiResponse res = bridgeClient.contractDetail(body);
        log.info("[MERITZ][CONTRACT_DETAIL] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            throw new MeritzContractClientException(
                    "계약조회 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
        return new BridgeApiResult(res.isSuccess(), res.getErrCd(), res.getErrMsg(), res.getData());
    }

    // ── private helpers ──

    private Map<String, Object> companyBody(CompanyConfig cfg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("company", cfg.getCompanyCode());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        return body;
    }

    private JsonNode parseData(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("[MERITZ] 응답 데이터 파싱 실패", e);
            throw new MeritzContractClientException("응답 데이터 파싱 실패");
        }
    }

    private BigDecimal parseBigDecimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) return null;
        try {
            return new BigDecimal(value.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
