package com.nexsol.tpa.core.api.controller.v1.response;

import java.time.LocalDate;

import com.nexsol.tpa.core.domain.subscription.SubscriptionResult;

public record ContractCompletedResponse(
        boolean ok,
        String errCd,
        String errMsg,
        Long contractId,
        String insuranceProductName,
        String planName,
        LocalDate insureStartDate,
        LocalDate insureEndDate,
        String contractPeopleName,
        int insuredPeopleCount) {

    public static ContractCompletedResponse of(SubscriptionResult r) {
        return new ContractCompletedResponse(
                r.ok(),
                r.errCd(),
                r.errMsg(),
                r.contractId(),
                r.insuranceProductName(),
                r.planName(),
                r.insureStartDate(),
                r.insureEndDate(),
                r.contractPeopleName(),
                r.insuredPeopleCount());
    }
}