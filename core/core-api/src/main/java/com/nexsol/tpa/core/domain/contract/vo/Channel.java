package com.nexsol.tpa.core.domain.contract.vo;

import com.nexsol.tpa.storage.db.core.entity.TpaChannelEntity;

import lombok.Builder;

@Builder
public record Channel(Long id, String code, String name) {

    public static Channel of(TpaChannelEntity e) {
        if (e == null) return null;
        return Channel.builder()
                .id(e.getId())
                .code(e.getChannelCode())
                .name(e.getChannelName())
                .build();
    }
}