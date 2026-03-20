package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.plan.Insurer;
import com.nexsol.tpa.core.domain.repository.InsurerRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaInsurerRepository;

@Repository
@RequiredArgsConstructor
public class DefaultInsurerRepository implements InsurerRepository {

    private final JpaInsurerRepository jpaRepository;

    @Override
    public Optional<Insurer> findById(Long id) {
        return jpaRepository.findById(id).map(TravelInsurerEntity::toDomain);
    }
}