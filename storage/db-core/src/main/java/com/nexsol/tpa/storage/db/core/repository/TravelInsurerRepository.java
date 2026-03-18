package com.nexsol.tpa.storage.db.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;

public interface TravelInsurerRepository extends JpaRepository<TravelInsurerEntity, Long> {}
