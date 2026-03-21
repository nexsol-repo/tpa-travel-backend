package com.nexsol.tpa.core.domain.repository;

import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.city.FavoriteCity;

public interface FavoriteCityRepository {
    FavoriteCity save(FavoriteCity city);

    List<FavoriteCity> findAllActive();

    Optional<FavoriteCity> findActiveById(Long id);
}
