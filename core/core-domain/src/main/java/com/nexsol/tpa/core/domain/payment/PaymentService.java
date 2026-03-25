package com.nexsol.tpa.core.domain.payment;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.PaymentProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProvider paymentProvider;

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
        return paymentProvider.approveCard(
                company, polNo, quotGrpNo, quotReqNo, crdNo, efctPrd, dporNm, dporCd, rcptPrem);
    }

    public CardCancellation cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt) {
        return paymentProvider.cancelCard(company, polNo, estNo, orgApvNo, cncAmt);
    }
}
