package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

public interface TravelInsuredRepository extends JpaRepository<TravelInsuredEntity, Long> {

    List<TravelInsuredEntity> findByContractIdAndDeletedAtIsNullOrderByIdAsc(Long contractId);

    TravelInsuredEntity findByContractIdAndIsContractorTrueAndDeletedAtIsNull(Long contractId);

    @Query(
            """
                select p
                from TravelInsuredEntity p
                where p.contractId in :contractIds
                  and p.deletedAt is null
                order by p.contractId asc, p.id asc
            """)
    List<TravelInsuredEntity> findByContractIds(@Param("contractIds") Collection<Long> contractIds);
}
