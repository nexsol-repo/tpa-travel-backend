package com.nexsol.tpa.core.domain.contract;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record AuthInfo(String uniqueKey, String status, LocalDateTime date) {}
