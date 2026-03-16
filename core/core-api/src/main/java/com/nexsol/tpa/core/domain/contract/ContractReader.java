package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelContractRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractReader {

    private final TravelContractRepository contractRepository;

    public TravelContractEntity getById(Long id) {
        return contractRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("contract not found: " + id));
    }
}
