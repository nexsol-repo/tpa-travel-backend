package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.plan.PlanFinder;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.core.domain.refund.RefundReader;
import com.nexsol.tpa.core.support.PageResult;
import com.nexsol.tpa.core.support.SortPage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractQueryService {

    private final ContractFinder contractFinder;
    private final ContractPaymentFinder paymentFinder;
    private final ContractPeopleFinder peopleFinder;
    private final PlanFinder planFinder;
    private final PlanReader planReader;
    private final RefundReader refundReader;

    public PageResult<ContractDetail> list(String authUniqueKey, int page, int size) {
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
        var planMap = planFinder.findMapByIds(planIds);

        List<ContractDetail> items =
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
                                    return ContractDetail.builder()
                                            .contract(c)
                                            .payment(payMap.get(c.id()))
                                            .people(people)
                                            .plan(repPlanId != null ? planMap.get(repPlanId) : null)
                                            .build();
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
        var plan = planReader.readById(repPlanId);

        var refund = refundReader.readByContractId(id);

        return ContractDetail.builder()
                .contract(contract)
                .payment(payment)
                .people(people)
                .plan(plan)
                .refund(refund)
                .build();
    }
}
