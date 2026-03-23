package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.api.controller.v1.response.ContractDetail;
import com.nexsol.tpa.core.api.controller.v1.response.ContractListItem;
import com.nexsol.tpa.core.api.controller.v1.response.ContractQueryResponse;
import com.nexsol.tpa.core.support.PageResult;
import com.nexsol.tpa.core.support.SortPage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelContractQueryService {

    private final ContractFinder contractFinder;
    private final ContractPaymentFinder paymentFinder;
    private final ContractPeopleFinder peopleFinder;
    private final ContractReferenceFinder referenceFinder;

    public PageResult<ContractListItem> list(String authUniqueKey, int page, int size) {
        SortPage sortPage =
                new SortPage(Math.max(page, 0), Math.min(Math.max(size, 1), 100), null, null);

        PageResult<ContractInfo> contracts = contractFinder.find(authUniqueKey, sortPage);

        List<Long> contractIds = contracts.getContent().stream().map(ContractInfo::id).toList();

        var payMap = paymentFinder.findMapByContractIds(contractIds);
        var peopleMap = peopleFinder.findGroupByContractIds(contractIds);

        List<Long> planIds =
                peopleMap.values().stream()
                        .flatMap(List::stream)
                        .map(InsuredPerson::planId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        var planMap = referenceFinder.findPlanMapByIds(planIds);

        List<ContractListItem> items =
                contracts.getContent().stream()
                        .map(
                                c -> {
                                    var people = peopleMap.getOrDefault(c.id(), List.of());
                                    var repPlanId =
                                            people.stream()
                                                    .map(InsuredPerson::planId)
                                                    .filter(Objects::nonNull)
                                                    .findFirst()
                                                    .orElse(null);
                                    return ContractQueryResponse.of(
                                            c,
                                            payMap.get(c.id()),
                                            repPlanId != null ? planMap.get(repPlanId) : null,
                                            people);
                                })
                        .toList();

        return PageResult.of(items, contracts.getTotalElements(), sortPage.size(), sortPage.page());
    }

    public ContractDetail get(Long id) {
        var contract = contractFinder.findById(id);
        var payment = paymentFinder.findByContractId(id);
        var people = peopleFinder.findByContractId(id);

        var repPlanId =
                people.stream()
                        .map(InsuredPerson::planId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
        var plan = referenceFinder.findPlan(repPlanId);
        var insurer = referenceFinder.findInsurer(contract.insurerId());
        var partner = referenceFinder.findPartner(contract.partnerId());
        var channel = referenceFinder.findChannel(contract.channelId());

        return ContractQueryResponse.of(contract, payment, people, plan, insurer, partner, channel);
    }
}
