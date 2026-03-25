package com.nexsol.tpa.core.domain.client;

import com.nexsol.tpa.core.domain.payment.CardApproval;
import com.nexsol.tpa.core.domain.payment.CardCancellation;

public interface PaymentProvider {

    CardApproval approveCard(
            String company,
            String polNo,
            String quotGrpNo,
            String quotReqNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String rcptPrem);

    CardCancellation cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt);
}
