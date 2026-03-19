package com.nexsol.tpa.core.domain.subscription;

import java.time.LocalDate;

public record SubscriptionResult(
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

    public static SubscriptionResult success(
            Long contractId,
            String insuranceProductName,
            String planName,
            LocalDate insureStartDate,
            LocalDate insureEndDate,
            String contractPeopleName,
            int insuredPeopleCount) {

        return new SubscriptionResult(
                true, "00001", "0",
                contractId, insuranceProductName, planName,
                insureStartDate, insureEndDate,
                contractPeopleName, insuredPeopleCount);
    }

    public static SubscriptionResult fail(String errCd, String errMsg) {
        return new SubscriptionResult(
                false, errCd, errMsg,
                null, null, null, null, null, null, 0);
    }
}