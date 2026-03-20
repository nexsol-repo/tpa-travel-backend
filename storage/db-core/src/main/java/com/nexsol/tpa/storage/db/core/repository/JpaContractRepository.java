package com.nexsol.tpa.storage.db.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

public interface JpaContractRepository extends JpaRepository<TravelContractEntity, Long> {

    @Query(
            """
                select c
                from TravelContractEntity c
                where c.status <> com.nexsol.tpa.core.enums.TravelContractStatus.PENDING
                order by
                  case when c.authUniqueKey is null then 1 else 0 end asc,
                  c.authUniqueKey desc,
                  c.id desc
            """)
    Page<TravelContractEntity> findAllOrderByAuthUniqueKeyDesc(Pageable pageable);

    @Query(
            """
                select c
                from TravelContractEntity c
                where c.authUniqueKey = :authUniqueKey
                  and c.status <> com.nexsol.tpa.core.enums.TravelContractStatus.PENDING
                order by c.id desc
            """)
    Page<TravelContractEntity> findByAuthUniqueKey(String authUniqueKey, Pageable pageable);
}
