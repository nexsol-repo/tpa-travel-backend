package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.contract.Channel;
import com.nexsol.tpa.core.domain.repository.ChannelRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaChannelEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaChannelRepository;

@Repository
@RequiredArgsConstructor
public class DefaultChannelRepository implements ChannelRepository {

    private final JpaChannelRepository jpaRepository;

    @Override
    public Optional<Channel> findById(Long id) {
        return jpaRepository.findById(id).map(TpaChannelEntity::toDomain);
    }

    @Override
    public Optional<Channel> findByChannelCode(String code) {
        return jpaRepository.findByChannelCode(code).map(TpaChannelEntity::toDomain);
    }

    @Override
    public List<Channel> findAllActive() {
        return jpaRepository.findAllByIsActiveTrueOrderByIdAsc()
                .stream().map(TpaChannelEntity::toDomain).toList();
    }

    @Override
    public List<Channel> findActiveByPartnerId(Long partnerId) {
        return jpaRepository.findAllByPartnerIdAndIsActiveTrueOrderByIdAsc(partnerId)
                .stream().map(TpaChannelEntity::toDomain).toList();
    }
}