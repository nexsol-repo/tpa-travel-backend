package com.nexsol.tpa.core.domain.repository;

import java.util.Optional;

import com.nexsol.tpa.core.domain.plan.Insurer;

public interface InsurerRepository {
    Optional<Insurer> findById(Long id);
}
