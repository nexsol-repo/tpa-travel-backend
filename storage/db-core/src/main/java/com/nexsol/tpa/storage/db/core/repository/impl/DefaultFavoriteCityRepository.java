package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.FavoriteCityRepository;
import com.nexsol.tpa.storage.db.core.entity.FavoriteCityEntity;

@Repository
@RequiredArgsConstructor
public class DefaultFavoriteCityRepository implements FavoriteCityRepository {

    private final com.nexsol.tpa.storage.db.core.repository.JpaFavoriteCityRepository jpaRepository;

    @Override
    public FavoriteCityEntity save(FavoriteCityEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public List<FavoriteCityEntity> findAllActive() {
        return jpaRepository.findByDeletedAtIsNullOrderBySortOrderAsc();
    }

    @Override
    public Optional<FavoriteCityEntity> findActiveById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id);
    }
}