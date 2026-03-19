package com.nexsol.tpa.core.domain.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResult(
        boolean ok,
        String errCd,
        String errMsg,
        ContractInfo contract,
        InsurerInfo insurer,
        PlanInfo plan) {

    public static SubscriptionResult success(
            ContractInfo contract, InsurerInfo insurer, PlanInfo plan) {
        return new SubscriptionResult(true, "00001", "0", contract, insurer, plan);
    }

    public static SubscriptionResult fail(String errCd, String errMsg) {
        return new SubscriptionResult(false, errCd, errMsg, null, null, null);
    }

    public record ContractInfo(
            Long id,
            Long partnerId,
            Long channelId,
            Long planId,
            Long familyId,
            String policyNumber,
            String meritzQuoteGroupNumber,
            String meritzQuoteRequestNumber,
            String countryName,
            String countryCode,
            BigDecimal totalFee,
            String status,
            LocalDate insureBeginDate,
            LocalDate insureEndDate,
            String contractPeopleName,
            String contractPeopleHp,
            String contractPeopleMail) {}

    public record InsurerInfo(Long id, String name, String code) {}

    public record PlanInfo(
            Long id,
            String insuranceProductName,
            String planName,
            String productCode,
            String unitProductCode,
            String planGroupCode,
            String planCode) {}
}
