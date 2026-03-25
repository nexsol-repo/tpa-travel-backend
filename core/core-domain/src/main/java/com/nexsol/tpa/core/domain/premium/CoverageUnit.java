package com.nexsol.tpa.core.domain.premium;

public record CoverageUnit(
        String ageBandCode, String ageBandLabel, Integer count, Long insdAmt, Long premSum) {}
