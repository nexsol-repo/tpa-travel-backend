package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.FavoriteCityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteCityRepository extends JpaRepository<FavoriteCityEntity, Long> {

    List<FavoriteCityEntity> findByDeletedAtIsNullOrderBySortOrderAsc();

    Optional<FavoriteCityEntity> findByIdAndDeletedAtIsNull(Long id);

}
