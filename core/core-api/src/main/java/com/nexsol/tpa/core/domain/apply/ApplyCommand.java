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
        Long familyId,
        String meritzQuoteGroupNumber,
        String meritzQuoteRequestNumber,
        String countryCode,
        String countryName,
        LocalDate insureBeginDate,
        LocalDate insureEndDate,
        BigDecimal totalPremium,
        List<InsuredPerson> people,
        boolean marketingConsentUsed) {

    public record InsuredPerson(
            Long planId,
            String name,
            String gender,
            String residentNumber,
            String englishName,
            String passportNumber,
            String phone,
            String email,
            String insureNumber,
            BigDecimal insurePremium) {}
}
