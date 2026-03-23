package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record InsuredPerson(
        Long id,
        Long contractId,
        Long planId,
        boolean isContractor,
        String name,
        String englishName,
        String gender,
        String residentNumber,
        String passportNumber,
        String phone,
        String email,
        BigDecimal insurePremium) {}
