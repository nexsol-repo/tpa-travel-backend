package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ContractInfo(
        Long id,
        Long familyId,
        String policyNumber,
        String meritzQuoteGroupNumber,
        String meritzQuoteRequestNumber,
        BigDecimal totalPremium,
        String policyLink,
        String status,
        LocalDateTime applyDate,
        InsurePeriod insurePeriod,
        Contractor contractor,
        AuthInfo auth,
        boolean marketingConsentUsed,
        Long employeeId) {}
