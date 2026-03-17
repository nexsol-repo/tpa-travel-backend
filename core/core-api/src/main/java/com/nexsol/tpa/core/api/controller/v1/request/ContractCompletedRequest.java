package com.nexsol.tpa.core.api.controller.v1.request;

import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ContractCompletedRequest {

    private final Long contractId;
    private final String cardNo;
    private final String efctPrd;
    private final String dporNm;
    private final String dporCd;

    @Builder
    private ContractCompletedRequest(
            Long contractId, String cardNo, String efctPrd, String dporNm, String dporCd) {
        this.contractId = contractId;
        this.cardNo = cardNo;
        this.efctPrd = efctPrd;
        this.dporNm = dporNm;
        this.dporCd = dporCd;
    }

    public SubscriptionCommand toSubscriptionCommand() {
        return new SubscriptionCommand(contractId, cardNo, efctPrd, dporNm, dporCd);
    }
}
