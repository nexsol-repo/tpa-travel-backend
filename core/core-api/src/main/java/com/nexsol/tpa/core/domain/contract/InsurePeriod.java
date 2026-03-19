package com.nexsol.tpa.core.domain.contract;

import java.time.LocalDate;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.Builder;

@Builder
public record InsurePeriod(
        LocalDate startDate, LocalDate endDate, String countryCode, String countryName) {

    public static InsurePeriod of(TravelContractEntity c) {
        return InsurePeriod.builder()
                .startDate(c.getInsureStartDate())
                .endDate(c.getInsureEndDate())
                .countryCode(c.getCountryCode())
                .countryName(c.getCountryName())
                .build();
    }
}
