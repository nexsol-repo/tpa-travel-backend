package com.nexsol.tpa.provider.meritz.payment;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.domain.client.InsurancePaymentClient;
import com.nexsol.tpa.core.domain.client.PaymentProvider;
import com.nexsol.tpa.core.domain.payment.CardApproval;
import com.nexsol.tpa.core.domain.payment.CardCancellation;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzPaymentProvider implements PaymentProvider {

    private final InsurancePaymentClient paymentClient;
    private final ObjectMapper objectMapper;

    @Override
    public CardApproval approveCard(
            String company,
            String polNo,
            String quotGrpNo,
            String quotReqNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String rcptPrem) {

        BridgeApiResult res =
                paymentClient.approveCard(
                        company, polNo, quotGrpNo, quotReqNo, crdNo, efctPrd, dporNm, dporCd,
                        rcptPrem);

        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "카드승인 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }

        JsonNode data = parseData(res.data());
        return CardApproval.builder()
                .approvalNumber(data.path("crdApvNo").asText(null))
                .approvalDate(data.path("crdApvDt").asText(null))
                .build();
    }

    @Override
    public CardCancellation cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt) {

        BridgeApiResult res = paymentClient.cancelCard(company, polNo, estNo, orgApvNo, cncAmt);

        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "카드취소 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }

        JsonNode data = parseData(res.data());
        return CardCancellation.builder()
                .cancellationNumber(data.path("crdCncNo").asText(null))
                .build();
    }

    private JsonNode parseData(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("[MERITZ_PAYMENT] 응답 파싱 실패", e);
            throw new CoreException(CoreErrorType.DEFAULT_ERROR, "결제 응답 파싱 실패");
        }
    }
}
