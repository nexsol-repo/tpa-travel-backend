package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsuredRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPeopleFinder {

    private final TravelInsuredRepository peopleRepository;

    public List<TravelInsuredEntity> findByContractId(Long contractId) {
        return peopleRepository.findByContractIdAndDeletedAtIsNullOrderByIdAsc(contractId);
    }

    public TravelInsuredEntity findContractor(Long contractId) {
        return peopleRepository.findByContractIdAndIsContractorTrueAndDeletedAtIsNull(contractId);
    }

    public Map<Long, List<TravelInsuredEntity>> findGroupByContractIds(List<Long> contractIds) {
        return peopleRepository.findByContractIds(contractIds).stream()
                .collect(Collectors.groupingBy(TravelInsuredEntity::getContractId));
    }
}
