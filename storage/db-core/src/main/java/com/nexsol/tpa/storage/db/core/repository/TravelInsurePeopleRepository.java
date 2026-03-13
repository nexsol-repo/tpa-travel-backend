package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurePeopleEntity;

public interface TravelInsurePeopleRepository
        extends JpaRepository<TravelInsurePeopleEntity, Long> {

    List<TravelInsurePeopleEntity> findByContractIdOrderByIdAsc(Long contractId);

    @Query(
            """
                select p
                from TravelInsurePeopleEntity p
                where p.contractId in :contractIds
                order by p.contractId asc, p.id asc
            """)
    List<TravelInsurePeopleEntity> findByContractIds(
            @Param("contractIds") Collection<Long> contractIds);
}
