package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.nexsol.tpa.core.domain.apply.ApplyCommand;

public record ContractApplyRequest(
        Long insurerId,
        String insuerName,
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
        String contractPeopleName,
        String contractPeopleResidentNumber,
        String contractPeopleHp,
        String contractPeopleMail,
        BigDecimal totalFee,
        List<InsurePeopleRequest> people,
        boolean marketingConsentUsed) {

    public ApplyCommand toCommand() {
        List<ApplyCommand.InsuredPerson> insuredPeople =
                people == null
                        ? List.of()
                        : people.stream()
                                .map(
                                        p ->
                                                new ApplyCommand.InsuredPerson(
                                                        p.planId(),
                                                        p.name(),
                                                        p.gender(),
                                                        p.residentNumber(),
                                                        p.nameEng(),
                                                        p.passportNumber(),
                                                        p.insureNumber(),
                                                        p.insurePremium()))
                                .toList();

        return new ApplyCommand(
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
                contractPeopleName,
                contractPeopleResidentNumber,
                contractPeopleHp,
                contractPeopleMail,
                totalFee,
                insuredPeople,
                marketingConsentUsed);
    }
}
