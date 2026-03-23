package com.nexsol.tpa.core.domain.contract;

import lombok.Builder;

@Builder
public record Contractor(String name, String residentNumberMasked, String phone, String email) {}
