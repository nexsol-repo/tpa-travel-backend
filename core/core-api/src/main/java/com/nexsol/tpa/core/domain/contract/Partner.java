package com.nexsol.tpa.core.domain.contract;

import lombok.Builder;

@Builder
public record Partner(Long id, String code, String name) {}
