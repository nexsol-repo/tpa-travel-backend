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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzContractClient {

    private final MeritzBridgeFeignClient bridgeClient;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    // ── estimateSave (가입확정) ──

    public SubscriptionApiResult estimateSave(EstimateSaveRequest req) {
        CompanyConfig cfg = companies.resolve(req.company());

        Map<String, Object> body = companyBody(cfg);
        body.put("polNo", req.polNo());
        body.put("pdCd", req.pdCd());
        body.put("untPdCd", req.untPdCd());
        body.put("sbcpDt", req.sbcpDt());
        body.put("insBgnDt", req.insBgnDt());
        body.put("insEdDt", req.insEdDt());
        body.put("trvArCd", req.trvArCd());
        body.put("inspeCnt", req.insuredPeople().size());

        List<Map<String, Object>> insuredVos =
                req.insuredPeople().stream()
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

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("crdNo", req.cardNo());
        card.put("efctPrd", req.efctPrd());
        card.put("dporNm", req.dporNm());
        card.put("dporCd", req.dporCd());
        body.put("ctrTrsInfBcVo", card);

        MeritzBridgeApiResponse res = bridgeClient.estimateSave(body);
        log.info("[MERITZ][EST_SAVE] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            return SubscriptionApiResult.fail(res.getErrCd(), res.getErrMsg(), res);
        }

        JsonNode data = parseData(res.getData());
        return SubscriptionApiResult.success(
                parseBigDecimal(data, "ttPrem"),
                data.path("polNo").asText(null),
                data.path("quotGrpNo").asText(null),
                data.path("quotReqNo").asText(null),
                res);
    }

    // ── 계약 취소 ──

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

    public CertificateApiResult issueCertificate(
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
        return new CertificateApiResult(data.path("rltLinkUrl").asText(null));
    }

    public MeritzBridgeApiResponse issueCertificateRaw(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd) {

        return callCertificate(company, polNo, pdCd, quotGrpNo, quotReqNo, otptDiv, otptTpCd);
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

    public MeritzBridgeApiResponse contractList(String company, Map<String, Object> bodyFields) {
        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = companyBody(cfg);
        body.putAll(bodyFields);

        MeritzBridgeApiResponse res = bridgeClient.contractList(body);
        log.info("[MERITZ][CONTRACT_LIST] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            throw new MeritzContractClientException(
                    "계약목록조회 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
        return res;
    }

    public MeritzBridgeApiResponse contractDetail(String company, Map<String, Object> bodyFields) {
        CompanyConfig cfg = companies.resolve(company);

        Map<String, Object> body = companyBody(cfg);
        body.putAll(bodyFields);

        MeritzBridgeApiResponse res = bridgeClient.contractDetail(body);
        log.info("[MERITZ][CONTRACT_DETAIL] success={}, errCd={}", res.isSuccess(), res.getErrCd());

        if (!res.isSuccess()) {
            throw new MeritzContractClientException(
                    "계약조회 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
        return res;
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

    // ── estimateSave 요청 DTO ──

    public record EstimateSaveRequest(
            String company,
            String polNo,
            String pdCd,
            String untPdCd,
            String sbcpDt,
            String insBgnDt,
            String insEdDt,
            String trvArCd,
            String cardNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            List<InsuredPerson> insuredPeople) {

        public record InsuredPerson(
                String inspeBdt,
                String gndrCd,
                String inspeNm,
                String engInspeNm,
                String planGrpCd,
                String planCd) {}
    }
}
