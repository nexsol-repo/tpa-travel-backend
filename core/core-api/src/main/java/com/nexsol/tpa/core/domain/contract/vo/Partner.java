package com.nexsol.tpa.core.domain.contract.vo;

import com.nexsol.tpa.storage.db.core.entity.TpaPartnerEntity;

import lombok.Builder;

@Builder
public record Partner(Long id, String code, String name) {

    public static Partner of(TpaPartnerEntity e) {
        if (e == null) return null;
        return Partner.builder()
                .id(e.getId())
                .code(e.getPartnerCode())
                .name(e.getPartnerName())
                .build();
    }
}