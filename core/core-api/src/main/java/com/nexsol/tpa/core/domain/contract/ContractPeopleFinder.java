package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurePeopleEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurePeopleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPeopleFinder {

    private final TravelInsurePeopleRepository peopleRepository;

    public List<TravelInsurePeopleEntity> findByContractId(Long contractId) {
        return peopleRepository.findByContractIdAndDeletedAtIsNullOrderByIdAsc(contractId);
    }

    public TravelInsurePeopleEntity findContractor(Long contractId) {
        return peopleRepository.findByContractIdAndIsContractorTrueAndDeletedAtIsNull(contractId);
    }

    public Map<Long, List<TravelInsurePeopleEntity>> findGroupByContractIds(
            List<Long> contractIds) {
        return peopleRepository.findByContractIds(contractIds).stream()
                .collect(Collectors.groupingBy(TravelInsurePeopleEntity::getContractId));
    }
}
