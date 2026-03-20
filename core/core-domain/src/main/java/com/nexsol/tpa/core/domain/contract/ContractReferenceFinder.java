package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;
import com.nexsol.tpa.core.domain.repository.ChannelRepository;
import com.nexsol.tpa.core.domain.repository.PartnerRepository;
import com.nexsol.tpa.core.domain.repository.InsurancePlanRepository;
import com.nexsol.tpa.core.domain.repository.InsurerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractReferenceFinder {

    private final InsurancePlanRepository planRepository;
    private final InsurerRepository insurerRepository;
    private final PartnerRepository partnerRepository;
    private final ChannelRepository channelRepository;

    public Map<Long, InsurancePlan> findPlanMapByIds(List<Long> planIds) {
        return planRepository.findByIdIn(planIds).stream()
                .collect(Collectors.toMap(InsurancePlan::id, p -> p));
    }

    public InsurancePlan findPlan(Long planId) {
        if (planId == null) return null;
        return planRepository.findById(planId).orElse(null);
    }

    public Insurer findInsurer(Long insurerId) {
        if (insurerId == null) return null;
        return insurerRepository.findById(insurerId).orElse(null);
    }

    public Partner findPartner(Long partnerId) {
        if (partnerId == null) return null;
        return partnerRepository.findById(partnerId).orElse(null);
    }

    public Channel findChannel(Long channelId) {
        if (channelId == null) return null;
        return channelRepository.findById(channelId).orElse(null);
    }
}