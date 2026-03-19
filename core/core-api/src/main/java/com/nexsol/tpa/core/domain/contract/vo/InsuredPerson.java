package com.nexsol.tpa.core.domain.contract.vo;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import java.math.BigDecimal;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurePeopleEntity;

import lombok.Builder;

@Builder
public record InsuredPerson(
        Long id,
        Long planId,
        boolean isContractor,
        String name,
        String nameEng,
        String gender,
        String residentNumberMasked,
        String passportNumberMasked,
        String policyNumber,
        BigDecimal insurePremium) {

    public static InsuredPerson of(TravelInsurePeopleEntity e) {
        return InsuredPerson.builder()
                .id(e.getId())
                .planId(e.getPlanId())
                .isContractor(Boolean.TRUE.equals(e.getIsContractor()))
                .name(e.getName())
                .nameEng(e.getNameEng())
                .gender(e.getGender())
                .residentNumberMasked(maskRrn(e.getResidentNumber()))
                .passportNumberMasked(maskPassport(e.getPassportNumber()))
                .policyNumber(e.getPolicyNumber())
                .insurePremium(e.getInsurePremium())
                .build();
    }
}