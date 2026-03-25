package com.nexsol.tpa.core.domain.payment;

import lombok.Builder;

@Builder
public record CardCancellation(String cancellationNumber) {}
