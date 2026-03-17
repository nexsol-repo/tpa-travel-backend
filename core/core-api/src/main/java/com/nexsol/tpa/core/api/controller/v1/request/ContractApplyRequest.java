package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.nexsol.tpa.core.domain.apply.ApplyCommand;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ContractApplyRequest {

    private final Long insurerId;
    private final String insuerName;
    private final Long partnerId;
    private final String partnerName;
    private final Long channelId;
    private final String channelName;
    private final Long planId;
    private final String policyNumber;
    private final String meritzQuoteGroupNumber;
    private final String meritzQuoteRequestNumber;
    private final String countryCode;
    private final String countryName;
    private final LocalDate insureBeginDate;
    private final LocalDate insureEndDate;
    private final String contractPeopleName;
    private final String contractPeopleResidentNumber;
    private final String contractPeopleHp;
    private final String contractPeopleMail;
    private final BigDecimal totalFee;
    private final List<InsurePeopleRequest> people;
    private final boolean marketingConsentUsed;

    @Builder
    private ContractApplyRequest(
            Long insurerId,
            String insuerName,
            Long partnerId,
            String partnerName,
            Long channelId,
            String channelName,
            Long planId,
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
        this.insurerId = insurerId;
        this.insuerName = insuerName;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.channelId = channelId;
        this.channelName = channelName;
        this.planId = planId;
        this.policyNumber = policyNumber;
        this.meritzQuoteGroupNumber = meritzQuoteGroupNumber;
        this.meritzQuoteRequestNumber = meritzQuoteRequestNumber;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.insureBeginDate = insureBeginDate;
        this.insureEndDate = insureEndDate;
        this.contractPeopleName = contractPeopleName;
        this.contractPeopleResidentNumber = contractPeopleResidentNumber;
        this.contractPeopleHp = contractPeopleHp;
        this.contractPeopleMail = contractPeopleMail;
        this.totalFee = totalFee;
        this.people = people;
        this.marketingConsentUsed = marketingConsentUsed;
    }

    public ApplyCommand toCommand() {
        List<ApplyCommand.InsuredPerson> insuredPeople =
                people == null
                        ? List.of()
                        : people.stream()
                                .map(
                                        p ->
                                                new ApplyCommand.InsuredPerson(
                                                        p.getName(),
                                                        p.getGender(),
                                                        p.getResidentNumber(),
                                                        p.getNameEng(),
                                                        p.getPassportNumber(),
                                                        p.getInsureNumber(),
                                                        p.getInsurePremium()))
                                .toList();

        return new ApplyCommand(
                insurerId,
                partnerId,
                partnerName,
                channelId,
                channelName,
                planId,
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
