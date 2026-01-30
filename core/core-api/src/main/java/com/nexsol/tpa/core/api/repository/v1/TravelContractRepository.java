package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelContractRepository extends JpaRepository<TravelContractEntity, Long> {
}
