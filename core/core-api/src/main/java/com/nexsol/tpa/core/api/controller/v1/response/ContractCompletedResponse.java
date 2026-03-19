package com.nexsol.tpa.core.api.controller.v1.response;

import java.time.LocalDate;

import com.nexsol.tpa.core.domain.subscription.SubscriptionResult;

import lombok.Builder;

@Builder
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
        return ContractCompletedResponse.builder()
                .ok(r.ok())
                .errCd(r.errCd())
                .errMsg(r.errMsg())
                .contractId(r.contractId())
                .insuranceProductName(r.insuranceProductName())
                .planName(r.planName())
                .insureStartDate(r.insureStartDate())
                .insureEndDate(r.insureEndDate())
                .contractPeopleName(r.contractPeopleName())
                .insuredPeopleCount(r.insuredPeopleCount())
                .build();
    }
}
