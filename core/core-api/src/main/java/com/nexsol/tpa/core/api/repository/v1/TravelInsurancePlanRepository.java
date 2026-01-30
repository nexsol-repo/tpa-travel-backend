package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurancePlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TravelInsurancePlanRepository
        extends JpaRepository<TravelInsurancePlanEntity, Long> {

    @Query("""
                select p
                from TravelInsurancePlanEntity p
                where p.insurerId = :insurerId
                  and p.isActive = true
                order by p.sortOrder asc, p.id asc
            """)
    List<TravelInsurancePlanEntity> findActiveByInsurerId(
            @Param("insurerId") Long insurerId
    );

}

