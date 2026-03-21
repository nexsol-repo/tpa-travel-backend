package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.repository.ContractRepository;
import com.nexsol.tpa.core.support.PageResult;
import com.nexsol.tpa.core.support.SortPage;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaContractRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultContractRepository implements ContractRepository {

    private final JpaContractRepository jpaRepository;

    @Override
    public ContractInfo save(ContractInfo info) {
        TravelContractEntity entity = TravelContractEntity.fromDomain(info);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ContractInfo> findById(Long id) {
        return jpaRepository.findById(id).map(TravelContractEntity::toDomain);
    }

    @Override
    public PageResult<ContractInfo> findAllOrderByAuthUniqueKeyDesc(SortPage sortPage) {
        PageRequest pageable = PageRequest.of(sortPage.page(), sortPage.size());
        Page<TravelContractEntity> page = jpaRepository.findAllOrderByAuthUniqueKeyDesc(pageable);
        return PageResult.of(
                page.getContent().stream().map(TravelContractEntity::toDomain).toList(),
                page.getTotalElements(),
                sortPage.size(),
                sortPage.page());
    }

    @Override
    public PageResult<ContractInfo> findByAuthUniqueKey(String authUniqueKey, SortPage sortPage) {
        PageRequest pageable = PageRequest.of(sortPage.page(), sortPage.size());
        Page<TravelContractEntity> page =
                jpaRepository.findByAuthUniqueKey(authUniqueKey, pageable);
        return PageResult.of(
                page.getContent().stream().map(TravelContractEntity::toDomain).toList(),
                page.getTotalElements(),
                sortPage.size(),
                sortPage.page());
    }
}
