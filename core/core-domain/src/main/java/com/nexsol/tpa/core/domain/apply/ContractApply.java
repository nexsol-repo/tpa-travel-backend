package com.nexsol.tpa.core.domain.apply;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContractApply(
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
        List<NewInsuredPerson> people,
        boolean marketingConsentUsed) {}
