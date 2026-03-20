package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.ContractRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;
import com.nexsol.tpa.core.support.PageResult;
import com.nexsol.tpa.core.support.SortPage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractFinder {

    private final ContractRepository contractRepository;

    public PageResult<ContractInfo> find(String authUniqueKey, SortPage sortPage) {
        if (authUniqueKey != null && !authUniqueKey.isBlank()) {
            return contractRepository.findByAuthUniqueKey(authUniqueKey, sortPage);
        }
        return contractRepository.findAllOrderByAuthUniqueKeyDesc(sortPage);
    }

    public ContractInfo findById(Long id) {
        return contractRepository
                .findById(id)
                .orElseThrow(() -> new CoreException(CoreErrorType.NOT_FOUND_DATA, "contract not found: " + id));
    }
}