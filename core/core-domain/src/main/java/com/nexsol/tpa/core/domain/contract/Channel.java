package com.nexsol.tpa.core.domain.contract;

import lombok.Builder;

@Builder
public record Channel(Long id, String code, String name) {}
