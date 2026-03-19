package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexsol.tpa.storage.db.core.entity.TravelRefundEntity;

public interface TravelInsureRefundRepository extends JpaRepository<TravelRefundEntity, Long> {

    Optional<TravelRefundEntity> findByPaymentId(Long paymentId);

    Optional<TravelRefundEntity> findByContractId(Long contractId);

    @Query(
            """
            select r
            from TravelRefundEntity r
            where r.contractId in :contractIds
            """)
    List<TravelRefundEntity> findByContractIdIn(@Param("contractIds") Collection<Long> contractIds);
}
