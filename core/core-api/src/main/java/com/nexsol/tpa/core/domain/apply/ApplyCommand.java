package com.nexsol.tpa.core.domain.apply;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ApplyCommand(
        Long insurerId,
        Long partnerId,
        String partnerName,
        Long channelId,
        String channelName,
        Long planId,
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
        List<InsuredPerson> people,
        boolean marketingConsentUsed) {

    public record InsuredPerson(
            String name,
            String gender,
            String residentNumber,
            String nameEng,
            String passportNumber,
            String insureNumber,
            BigDecimal insurePremium) {}
}
