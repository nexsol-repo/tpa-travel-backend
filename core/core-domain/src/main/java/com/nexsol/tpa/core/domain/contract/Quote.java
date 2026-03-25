package com.nexsol.tpa.core.domain.contract;

import lombok.Builder;

@Builder
public record Quote(String groupNumber, String requestNumber) {}
