package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.ContractRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractReader {

    private final ContractRepository contractRepository;

    public ContractInfo getById(Long id) {
        return contractRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA, "contract not found: " + id));
    }
}
