package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsuredRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPeopleFinder {

    private final InsuredRepository peopleRepository;

    public List<InsuredPerson> findByContractId(Long contractId) {
        return peopleRepository.findByContractId(contractId);
    }

    public InsuredPerson findContractor(Long contractId) {
        return peopleRepository.findContractorByContractId(contractId).orElse(null);
    }

    public Map<Long, List<InsuredPerson>> findGroupByContractIds(List<Long> contractIds) {
        return peopleRepository.findByContractIds(contractIds).stream()
                .collect(Collectors.groupingBy(InsuredPerson::contractId));
    }
}
