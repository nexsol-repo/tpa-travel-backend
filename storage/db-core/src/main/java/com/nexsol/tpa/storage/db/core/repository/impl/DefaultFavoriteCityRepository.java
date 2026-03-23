package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.city.FavoriteCity;
import com.nexsol.tpa.core.domain.repository.FavoriteCityRepository;
import com.nexsol.tpa.storage.db.core.entity.FavoriteCityEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaFavoriteCityRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultFavoriteCityRepository implements FavoriteCityRepository {

    private final JpaFavoriteCityRepository jpaRepository;

    @Override
    public FavoriteCity save(FavoriteCity city) {
        FavoriteCityEntity entity = FavoriteCityEntity.fromDomain(city);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public List<FavoriteCity> findAllActive() {
        return jpaRepository.findByDeletedAtIsNullOrderBySortOrderAsc().stream()
                .map(FavoriteCityEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<FavoriteCity> findActiveById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id).map(FavoriteCityEntity::toDomain);
    }
}
