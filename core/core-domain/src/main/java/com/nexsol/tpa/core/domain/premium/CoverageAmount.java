package com.nexsol.tpa.core.domain.premium;

import java.util.List;

public record CoverageAmount(long insdAmt, String currency, List<CoverageUnit> units) {}
