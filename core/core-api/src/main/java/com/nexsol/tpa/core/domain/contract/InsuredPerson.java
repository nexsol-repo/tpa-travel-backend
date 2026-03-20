package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record InsuredPerson(
        Long id,
        Long planId,
        boolean isContractor,
        String name,
        String englishName,
        String gender,
        String residentNumberMasked,
        String passportNumberMasked,
        BigDecimal insurePremium) {}
