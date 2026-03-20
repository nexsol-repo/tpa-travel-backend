package com.nexsol.tpa.core.domain.contract;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record InsurePeriod(
        LocalDate startDate, LocalDate endDate, String countryCode, String countryName) {}
