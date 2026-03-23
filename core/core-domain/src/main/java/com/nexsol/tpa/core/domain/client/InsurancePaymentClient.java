package com.nexsol.tpa.core.domain.client;

public interface InsurancePaymentClient {

    InsuranceContractClient.BridgeApiResult approveCard(
            String company,
            String polNo,
            String quotGrpNo,
            String quotReqNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String rcptPrem);

    InsuranceContractClient.BridgeApiResult cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt);
}
