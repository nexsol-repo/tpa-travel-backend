package com.nexsol.tpa.core.api.controller.v1.request;

import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;

public record ContractCompletedRequest(
        Long contractId, String cardNo, String efctPrd, String dporNm, String dporCd) {

    public SubscriptionCommand toSubscriptionCommand() {
        return new SubscriptionCommand(contractId, cardNo, efctPrd, dporNm, dporCd);
    }
}
