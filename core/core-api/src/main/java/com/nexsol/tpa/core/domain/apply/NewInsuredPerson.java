package com.nexsol.tpa.core.domain.apply;

import java.math.BigDecimal;

public record NewInsuredPerson(
        Long planId,
        String name,
        String gender,
        String residentNumber,
        String englishName,
        String passportNumber,
        String phone,
        String email,
        BigDecimal insurePremium) {}
