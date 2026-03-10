package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurePaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TravelInsurePaymentRepository extends JpaRepository<TravelInsurePaymentEntity, Long> {

    Optional<TravelInsurePaymentEntity> findByContractId(Long contractId);

    boolean existsByContractId(Long contractId);

    @Query("""
            select p
            from TravelInsurePaymentEntity p
            where p.contractId in :contractIds
            """)
    List<TravelInsurePaymentEntity> findByContractIdIn(@Param("contractIds") Collection<Long> contractIds);

}
