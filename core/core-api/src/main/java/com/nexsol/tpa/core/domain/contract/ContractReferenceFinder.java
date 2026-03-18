package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TpaChannelEntity;
import com.nexsol.tpa.storage.db.core.entity.TpaPartnerEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;
import com.nexsol.tpa.storage.db.core.repository.TpaChannelRepository;
import com.nexsol.tpa.storage.db.core.repository.TpaPartnerRepository;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurancePlanRepository;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractReferenceFinder {

    private final TravelInsurancePlanRepository planRepository;
    private final TravelInsurerRepository insurerRepository;
    private final TpaPartnerRepository partnerRepository;
    private final TpaChannelRepository channelRepository;

    public Map<Long, TravelInsurancePlanEntity> findPlanMapByIds(List<Long> planIds) {
        return planRepository.findByIdIn(planIds).stream()
                .collect(Collectors.toMap(TravelInsurancePlanEntity::getId, p -> p));
    }

    public TravelInsurancePlanEntity findPlan(Long planId) {
        if (planId == null) return null;
        return planRepository.findById(planId).orElse(null);
    }

    public TravelInsurerEntity findInsurer(Long insurerId) {
        if (insurerId == null) return null;
        return insurerRepository.findById(insurerId).orElse(null);
    }

    public TpaPartnerEntity findPartner(Long partnerId) {
        if (partnerId == null) return null;
        return partnerRepository.findById(partnerId).orElse(null);
    }

    public TpaChannelEntity findChannel(Long channelId) {
        if (channelId == null) return null;
        return channelRepository.findById(channelId).orElse(null);
    }
}
