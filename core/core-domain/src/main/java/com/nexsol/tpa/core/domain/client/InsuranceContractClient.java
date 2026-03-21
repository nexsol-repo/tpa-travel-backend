package com.nexsol.tpa.core.domain.client;

import java.util.List;
import java.util.Map;

public interface InsuranceContractClient {

    SubscriptionResult estimateSave(EstimateSaveCommand command);

    void cancelContract(String company, String polNo, String quotGrpNo, String quotReqNo);

    CertificateLinkResult issueCertificate(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd);

    BridgeApiResult issueCertificateRaw(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd);

    BridgeApiResult contractList(String company, Map<String, Object> bodyFields);

    BridgeApiResult contractDetail(String company, Map<String, Object> bodyFields);

    // ── 개념객체 ──

    record SubscriptionResult(
            boolean success,
            java.math.BigDecimal ttPrem,
            String polNo,
            String quotGrpNo,
            String quotReqNo,
            String errCd,
            String errMsg,
            Object rawData) {

        public static SubscriptionResult success(
                java.math.BigDecimal ttPrem,
                String polNo,
                String quotGrpNo,
                String quotReqNo,
                Object rawData) {
            return new SubscriptionResult(
                    true, ttPrem, polNo, quotGrpNo, quotReqNo, null, null, rawData);
        }

        public static SubscriptionResult fail(String errCd, String errMsg, Object rawData) {
            return new SubscriptionResult(false, null, null, null, null, errCd, errMsg, rawData);
        }
    }

    record EstimateSaveCommand(
            String company,
            String polNo,
            String pdCd,
            String untPdCd,
            String sbcpDt,
            String insBgnDt,
            String insEdDt,
            String trvArCd,
            String grupSalChnDivCd,
            String cardNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            List<InsuredPersonCommand> insuredPeople) {

        public record InsuredPersonCommand(
                String inspeBdt,
                String gndrCd,
                String inspeNm,
                String engInspeNm,
                String planGrpCd,
                String planCd) {}
    }

    record CertificateLinkResult(String linkUrl) {}

    record BridgeApiResult(boolean success, String errCd, String errMsg, Object data) {}
}
