package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;

public record InsurePeopleRequest(
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
