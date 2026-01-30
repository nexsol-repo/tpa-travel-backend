package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurePaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelInsurePaymentRepository extends JpaRepository<TravelInsurePaymentEntity, Long> {
    Optional<TravelInsurePaymentEntity> findByContractId(Long contractId);
    boolean existsByContractId(Long contractId);
}
