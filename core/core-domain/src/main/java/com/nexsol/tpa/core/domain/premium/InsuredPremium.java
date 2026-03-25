package com.nexsol.tpa.core.domain.premium;

import lombok.Builder;

@Builder
public record InsuredPremium(
        Integer index,
        String currency,
        Long ppsPrem,
        String birth,
        String gndrCd,
        String cusNm,
        String cusEngNm) {}
