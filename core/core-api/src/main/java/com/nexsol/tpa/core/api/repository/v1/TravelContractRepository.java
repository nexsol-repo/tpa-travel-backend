package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelContractEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TravelContractRepository extends JpaRepository<TravelContractEntity, Long> {

    @Query("""
                select c
                from TravelContractEntity c
                order by
                  case when c.authUniqueKey is null then 1 else 0 end asc,
                  c.authUniqueKey desc,
                  c.id desc
            """)
    Page<TravelContractEntity> findAllOrderByAuthUniqueKeyDesc(Pageable pageable);

    @Query("""
                select c
                from TravelContractEntity c
                where c.authUniqueKey = :authUniqueKey
                order by c.id desc
            """)
    Page<TravelContractEntity> findByAuthUniqueKey(String authUniqueKey, Pageable pageable);

}
