package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.ChannelRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaChannelEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaChannelRepository;

@Repository
@RequiredArgsConstructor
public class DefaultChannelRepository implements ChannelRepository {

    private final JpaChannelRepository jpaRepository;

    @Override
    public Optional<TpaChannelEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<TpaChannelEntity> findByChannelCode(String code) {
        return jpaRepository.findByChannelCode(code);
    }

    @Override
    public List<TpaChannelEntity> findAllActive() {
        return jpaRepository.findAllByIsActiveTrueOrderByIdAsc();
    }

    @Override
    public List<TpaChannelEntity> findActiveByPartnerId(Long partnerId) {
        return jpaRepository.findAllByPartnerIdAndIsActiveTrueOrderByIdAsc(partnerId);
    }
}