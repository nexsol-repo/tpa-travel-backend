package com.nexsol.tpa.core.domain.contract;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

import lombok.Builder;

@Builder
public record Contractor(String name, String residentNumberMasked, String phone, String email) {

    public static Contractor of(TravelInsuredEntity insured) {
        if (insured == null) return null;
        return Contractor.builder()
                .name(insured.getName())
                .residentNumberMasked(maskRrn(insured.getResidentNumber()))
                .phone(insured.getPhone())
                .email(insured.getEmail())
                .build();
    }
}
