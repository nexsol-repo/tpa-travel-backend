package com.nexsol.tpa.core.domain.contract.vo;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.Builder;

@Builder
public record Contractor(
        String name,
        String residentNumberMasked,
        String hp,
        String mail) {

    public static Contractor of(TravelContractEntity c) {
        return Contractor.builder()
                .name(c.getContractPeopleName())
                .residentNumberMasked(maskRrn(c.getContractPeopleResidentNumber()))
                .hp(c.getContractPeopleHp())
                .mail(c.getContractPeopleMail())
                .build();
    }
}