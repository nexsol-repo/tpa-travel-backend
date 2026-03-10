package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurePeopleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TravelInsurePeopleRepository extends JpaRepository<TravelInsurePeopleEntity, Long> {

    // contract_id 단건 조회
    List<TravelInsurePeopleEntity> findByContract_IdOrderByIdAsc(Long contractId);

    // contract_id 여러건 조회 (목록 N+1 방지)
    @Query("""
                select p
                from TravelInsurePeopleEntity p
                where p.contract.id in :contractIds
                order by p.contract.id asc, p.id asc
            """)
    List<TravelInsurePeopleEntity> findByContractIds(@Param("contractIds") Collection<Long> contractIds);

}
