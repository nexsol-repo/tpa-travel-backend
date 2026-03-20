package com.nexsol.tpa.core.domain.plan;

import lombok.Builder;

@Builder
public record Insurer(Long id, String code, String name) {}
