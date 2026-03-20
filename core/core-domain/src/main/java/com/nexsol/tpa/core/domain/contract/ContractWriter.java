package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.ContractRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractWriter {

    private final ContractRepository contractRepository;

    public ContractInfo writerContract(ContractInfo contract) {
        return contractRepository.save(contract);
    }
}