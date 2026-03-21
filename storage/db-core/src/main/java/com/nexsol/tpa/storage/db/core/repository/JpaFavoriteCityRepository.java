package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.FavoriteCityEntity;

public interface JpaFavoriteCityRepository extends JpaRepository<FavoriteCityEntity, Long> {

    List<FavoriteCityEntity> findByDeletedAtIsNullOrderBySortOrderAsc();

    Optional<FavoriteCityEntity> findByIdAndDeletedAtIsNull(Long id);
}
