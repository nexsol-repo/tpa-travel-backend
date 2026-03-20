package com.nexsol.tpa.core.domain.contract;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelContractRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractFinder {

    private final TravelContractRepository contractRepository;

    public Page<TravelContractEntity> find(String authUniqueKey, Pageable pageable) {
        if (authUniqueKey != null && !authUniqueKey.isBlank()) {
            return contractRepository.findByAuthUniqueKey(authUniqueKey, pageable);
        }
        return contractRepository.findAllOrderByAuthUniqueKeyDesc(pageable);
    }

    public TravelContractEntity findById(Long id) {
        return contractRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("contract not found: " + id));
    }
}
