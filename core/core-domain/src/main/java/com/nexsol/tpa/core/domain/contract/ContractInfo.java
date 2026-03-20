package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ContractInfo(
        Long id,
        Long insurerId,
        String insurerName,
        Long partnerId,
        String partnerName,
        Long channelId,
        String channelName,
        Long familyId,
        String policyNumber,
        MeritzQuote meritzQuote,
        BigDecimal totalPremium,
        String policyLink,
        String status,
        LocalDateTime applyDate,
        InsurePeriod insurePeriod,
        AuthInfo auth,
        boolean marketingConsentUsed,
        Long employeeId) {}
