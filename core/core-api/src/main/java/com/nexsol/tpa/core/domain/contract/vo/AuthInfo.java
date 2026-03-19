package com.nexsol.tpa.core.domain.contract.vo;

import java.time.LocalDateTime;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.Builder;

@Builder
public record AuthInfo(
        String uniqueKey,
        String status,
        LocalDateTime date) {

    public static AuthInfo of(TravelContractEntity c) {
        return AuthInfo.builder()
                .uniqueKey(c.getAuthUniqueKey())
                .status(c.getAuthStatus())
                .date(c.getAuthDate())
                .build();
    }
}