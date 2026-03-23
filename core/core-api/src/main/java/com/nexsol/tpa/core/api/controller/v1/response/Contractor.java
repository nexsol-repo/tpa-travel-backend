package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;

@Builder
public record Contractor(String name, String residentNumberMasked, String phone, String email) {}
