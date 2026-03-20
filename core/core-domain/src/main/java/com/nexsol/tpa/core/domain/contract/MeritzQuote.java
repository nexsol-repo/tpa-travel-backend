package com.nexsol.tpa.core.domain.contract;

import lombok.Builder;

@Builder
public record MeritzQuote(String groupNumber, String requestNumber) {}