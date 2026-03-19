package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelPaymentEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurePaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPaymentFinder {

    private final TravelInsurePaymentRepository paymentRepository;

    public TravelPaymentEntity findByContractId(Long contractId) {
        return paymentRepository.findByContractId(contractId).orElse(null);
    }

    public Map<Long, TravelPaymentEntity> findMapByContractIds(List<Long> contractIds) {
        return paymentRepository.findByContractIdIn(contractIds).stream()
                .collect(Collectors.toMap(TravelPaymentEntity::getContractId, p -> p));
    }
}
