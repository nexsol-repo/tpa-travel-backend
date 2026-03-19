package com.nexsol.tpa.core.domain.plan;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;

import lombok.Builder;

@Builder
public record Insurer(Long id, String code, String name) {

    public static Insurer of(TravelInsurerEntity e) {
        if (e == null) return null;
        return Insurer.builder()
                .id(e.getId())
                .code(e.getInsurerCode())
                .name(e.getInsurerName())
                .build();
    }
}
