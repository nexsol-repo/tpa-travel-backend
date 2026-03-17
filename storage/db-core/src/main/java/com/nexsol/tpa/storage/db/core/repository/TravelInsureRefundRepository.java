package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexsol.tpa.storage.db.core.entity.TravelInsureRefundEntity;

public interface TravelInsureRefundRepository
        extends JpaRepository<TravelInsureRefundEntity, Long> {

    Optional<TravelInsureRefundEntity> findByPaymentId(Long paymentId);

    Optional<TravelInsureRefundEntity> findByContractId(Long contractId);

    @Query(
            """
            select r
            from TravelInsureRefundEntity r
            where r.contractId in :contractIds
            """)
    List<TravelInsureRefundEntity> findByContractIdIn(
            @Param("contractIds") Collection<Long> contractIds);
}
