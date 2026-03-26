package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.apply.ContractApply;

@Component
public class ContractCreator {

    public ContractInfo create(ContractApply cmd, String policyNumber) {
        return ContractInfo.builder()
                .insurerId(cmd.insurerId())
                .insurerName("MERITZ")
                .partnerId(cmd.partnerId())
                .partnerName(cmd.partnerName())
                .channelId(cmd.channelId())
                .channelName(cmd.channelName())
                .familyId(cmd.familyId())
                .policyNumber(policyNumber)
                .quote(
                        Quote.builder()
                                .groupNumber(cmd.meritzQuoteGroupNumber())
                                .requestNumber(cmd.meritzQuoteRequestNumber())
                                .build())
                .totalPremium(cmd.totalPremium())
                .status("PENDING")
                .insurePeriod(
                        InsurePeriod.builder()
                                .startDate(cmd.insureBeginDate())
                                .endDate(cmd.insureEndDate())
                                .countryCode(cmd.countryCode())
                                .countryName(cmd.countryName())
                                .build())
                .marketingConsentUsed(cmd.marketingConsentUsed())
                .build();
    }
}
