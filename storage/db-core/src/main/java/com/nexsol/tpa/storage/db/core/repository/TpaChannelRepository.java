package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TpaChannelEntity;

public interface TpaChannelRepository extends JpaRepository<TpaChannelEntity, Long> {

    Optional<TpaChannelEntity> findByChannelCode(String channelCode);

    List<TpaChannelEntity> findAllByIsActiveTrueOrderByIdAsc();

    List<TpaChannelEntity> findAllByPartnerIdAndIsActiveTrueOrderByIdAsc(Long partnerId);
}
