package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TpaChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TpaChannelRepository extends JpaRepository<TpaChannelEntity, Long> {

    Optional<TpaChannelEntity> findByChannelCode(String channelCode);

    List<TpaChannelEntity> findAllByIsActiveTrueOrderByIdAsc();

    List<TpaChannelEntity> findAllByPartnerIdAndIsActiveTrueOrderByIdAsc(Long partnerId);

}
