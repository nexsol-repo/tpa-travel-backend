package com.nexsol.tpa.core.domain.contract;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import java.math.BigDecimal;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

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
        String policyNumber,
        BigDecimal insurePremium) {

    public static InsuredPerson of(TravelInsuredEntity e) {
        return InsuredPerson.builder()
                .id(e.getId())
                .planId(e.getPlanId())
                .isContractor(Boolean.TRUE.equals(e.getIsContractor()))
                .name(e.getName())
                .englishName(e.getEnglishName())
                .gender(e.getGender())
                .residentNumberMasked(maskRrn(e.getResidentNumber()))
                .passportNumberMasked(maskPassport(e.getPassportNumber()))
                .policyNumber(e.getPolicyNumber())
                .insurePremium(e.getInsurePremium())
                .build();
    }
}
