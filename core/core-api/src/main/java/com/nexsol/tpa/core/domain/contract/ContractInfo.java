package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

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
        Long employeeId) {

    public static ContractInfo of(TravelContractEntity c, Contractor contractor) {
        return ContractInfo.builder()
                .id(c.getId())
                .familyId(c.getFamilyId())
                .policyNumber(c.getPolicyNumber())
                .meritzQuoteGroupNumber(c.getMeritzQuoteGroupNumber())
                .meritzQuoteRequestNumber(c.getMeritzQuoteRequestNumber())
                .totalPremium(c.getTotalPremium())
                .policyLink(c.getPolicyLink())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .applyDate(c.getApplyDate())
                .insurePeriod(InsurePeriod.of(c))
                .contractor(contractor)
                .auth(AuthInfo.of(c))
                .marketingConsentUsed(Boolean.TRUE.equals(c.getMarketingConsentUsed()))
                .employeeId(c.getEmployeeId())
                .build();
    }
}
