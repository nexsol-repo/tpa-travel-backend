package com.nexsol.tpa.core.domain.repository;

import java.util.Optional;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.support.PageResult;
import com.nexsol.tpa.core.support.SortPage;

public interface ContractRepository {
    ContractInfo save(ContractInfo info);

    Optional<ContractInfo> findById(Long id);

    PageResult<ContractInfo> findAllOrderByAuthUniqueKeyDesc(SortPage sortPage);

    PageResult<ContractInfo> findByAuthUniqueKey(String authUniqueKey, SortPage sortPage);
}
