package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record InsuredPersonDetail(
        Long id,
        Long planId,
        boolean isContractor,
        String name,
        String englishName,
        String gender,
        String residentNumberMasked,
        String passportNumberMasked,
        String phone,
        String email,
        BigDecimal insurePremium) {}
