package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;

public record InsurePeopleRequest(
        String name,
        String gender,
        String residentNumber,
        String nameEng,
        String passportNumber,
        String insureNumber,
        BigDecimal insurePremium) {}
