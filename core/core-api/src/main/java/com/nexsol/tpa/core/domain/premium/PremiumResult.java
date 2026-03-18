package com.nexsol.tpa.core.domain.premium;

import java.util.List;
import java.util.Map;

public record PremiumResult(
        long totalPremium,
        List<QuoteResult.InsuredPremium> insuredPremiums,
        Map<String, QuoteResult.CoverageAmount> coverageAmounts) {}
