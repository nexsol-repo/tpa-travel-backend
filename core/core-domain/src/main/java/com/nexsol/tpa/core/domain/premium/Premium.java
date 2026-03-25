package com.nexsol.tpa.core.domain.premium;

import java.util.List;
import java.util.Map;

public record Premium(
        long totalPremium,
        List<InsuredPremium> insuredPremiums,
        Map<String, CoverageAmount> coverageAmounts) {}
