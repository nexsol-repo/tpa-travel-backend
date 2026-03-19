package com.nexsol.tpa.core.domain.contract;

import static com.nexsol.tpa.core.api.controller.v1.response.ContractQueryResponse.*;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelContractQueryService {

    private final ContractFinder contractFinder;
    private final ContractPaymentFinder paymentFinder;
    private final ContractPeopleFinder peopleFinder;
    private final ContractReferenceFinder referenceFinder;

    @Transactional(readOnly = true)
    public Page<ContractListItem> list(String authUniqueKey, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));

        Page<TravelContractEntity> contracts = contractFinder.find(authUniqueKey, pageable);

        List<Long> contractIds =
                contracts.getContent().stream().map(TravelContractEntity::getId).toList();

        var payMap = paymentFinder.findMapByContractIds(contractIds);
        var peopleMap = peopleFinder.findGroupByContractIds(contractIds);

        // people에서 대표 planId 추출하여 plan 일괄 조회
        List<Long> planIds =
                peopleMap.values().stream()
                        .flatMap(List::stream)
                        .map(TravelInsuredEntity::getPlanId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        var planMap = referenceFinder.findPlanMapByIds(planIds);

        return contracts.map(
                c -> {
                    var people = peopleMap.getOrDefault(c.getId(), List.of());
                    var repPlanId =
                            people.stream()
                                    .map(TravelInsuredEntity::getPlanId)
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);
                    return ContractListItem.of(
                            c,
                            payMap.get(c.getId()),
                            repPlanId != null ? planMap.get(repPlanId) : null,
                            people);
                });
    }

    @Transactional(readOnly = true)
    public ContractDetail get(Long id) {
        var contract = contractFinder.findById(id);
        var payment = paymentFinder.findByContractId(id);
        var people = peopleFinder.findByContractId(id);

        var repPlanId =
                people.stream()
                        .map(TravelInsuredEntity::getPlanId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
        var plan = referenceFinder.findPlan(repPlanId);
        var insurer = referenceFinder.findInsurer(contract.getInsurerId());
        var partner = referenceFinder.findPartner(contract.getPartnerId());
        var channel = referenceFinder.findChannel(contract.getChannelId());

        return ContractDetail.of(contract, payment, people, plan, insurer, partner, channel);
    }
}
