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

        // 1) 계약 목록 조회
        Page<TravelContractEntity> contracts = contractFinder.find(authUniqueKey, pageable);

        // 2) 연관 데이터 일괄 조회
        List<Long> contractIds =
                contracts.getContent().stream().map(TravelContractEntity::getId).toList();
        List<Long> planIds =
                contracts.getContent().stream()
                        .map(TravelContractEntity::getPlanId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        var payMap = paymentFinder.findMapByContractIds(contractIds);
        var planMap = referenceFinder.findPlanMapByIds(planIds);
        var peopleMap = peopleFinder.findGroupByContractIds(contractIds);

        // 3) DTO 조립
        return contracts.map(
                c ->
                        ContractListItem.of(
                                c,
                                payMap.get(c.getId()),
                                planMap.get(c.getPlanId()),
                                peopleMap.getOrDefault(c.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public ContractDetail get(Long id) {
        // 1) 계약 조회
        var contract = contractFinder.findById(id);

        // 2) 연관 데이터 개별 조회
        var payment = paymentFinder.findByContractId(id);
        var people = peopleFinder.findByContractId(id);
        var plan = referenceFinder.findPlan(contract.getPlanId());
        var insurer = referenceFinder.findInsurer(contract.getInsurerId());
        var partner = referenceFinder.findPartner(contract.getPartnerId());
        var channel = referenceFinder.findChannel(contract.getChannelId());

        // 3) DTO 조립
        return ContractDetail.of(contract, payment, people, plan, insurer, partner, channel);
    }
}
