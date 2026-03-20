package com.nexsol.tpa.core.domain.repository;

import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.contract.Channel;

public interface ChannelRepository {
    Optional<Channel> findById(Long id);
    Optional<Channel> findByChannelCode(String code);
    List<Channel> findAllActive();
    List<Channel> findActiveByPartnerId(Long partnerId);
}