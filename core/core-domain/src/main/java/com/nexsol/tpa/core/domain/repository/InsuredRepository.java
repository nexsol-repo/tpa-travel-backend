package com.nexsol.tpa.core.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.contract.InsuredPerson;

public interface InsuredRepository {
    InsuredPerson save(InsuredPerson person);
    List<InsuredPerson> findByContractId(Long contractId);
    Optional<InsuredPerson> findContractorByContractId(Long contractId);
    List<InsuredPerson> findByContractIds(Collection<Long> contractIds);
}