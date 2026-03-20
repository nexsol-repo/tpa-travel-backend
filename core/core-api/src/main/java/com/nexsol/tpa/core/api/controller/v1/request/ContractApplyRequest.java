package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.nexsol.tpa.core.domain.apply.ContractApply;
import com.nexsol.tpa.core.domain.apply.NewInsuredPerson;

public record ContractApplyRequest(
        Long insurerId,
        Long partnerId,
        String partnerName,
        Long channelId,
        String channelName,
        Long familyId,
        String policyNumber,
        String meritzQuoteGroupNumber,
        String meritzQuoteRequestNumber,
        String countryCode,
        String countryName,
        LocalDate insureBeginDate,
        LocalDate insureEndDate,
        BigDecimal totalPremium,
        List<InsurePeopleRequest> people,
        boolean marketingConsentUsed) {

    public ContractApply toCommand() {
        List<NewInsuredPerson> insuredPeople =
                people == null
                        ? List.of()
                        : people.stream()
                                .map(
                                        p ->
                                                new NewInsuredPerson(
                                                        p.planId(),
                                                        p.name(),
                                                        p.gender(),
                                                        p.residentNumber(),
                                                        p.englishName(),
                                                        p.passportNumber(),
                                                        p.phone(),
                                                        p.email(),
                                                        p.insurePremium()))
                                .toList();

        return new ContractApply(
                insurerId,
                partnerId,
                partnerName,
                channelId,
                channelName,
                familyId,
                meritzQuoteGroupNumber,
                meritzQuoteRequestNumber,
                countryCode,
                countryName,
                insureBeginDate,
                insureEndDate,
                totalPremium,
                insuredPeople,
                marketingConsentUsed);
    }
}
