package com.nexsol.tpa.storage.db.core.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.repository.projection.PlanFamilyPlanRow;

public interface TravelInsurancePlanRepository
        extends JpaRepository<TravelInsurancePlanEntity, Long> {

    @Query(
            """
                select p
                from TravelInsurancePlanEntity p
                where p.insurerId = :insurerId
                  and p.isActive = true
                order by p.sortOrder asc, p.id asc
            """)
    List<TravelInsurancePlanEntity> findActiveByInsurerId(@Param("insurerId") Long insurerId);

    @Query(
            """
                select p
                from TravelInsurancePlanEntity p
                where p.id in (:ids)
            """)
    List<TravelInsurancePlanEntity> findByIdIn(@Param("ids") Collection<Long> ids);

    List<TravelInsurancePlanEntity> findByInsurerIdAndIsActiveTrueOrderBySortOrderAsc(
            Long insurerId);

    List<TravelInsurancePlanEntity> findByIdInAndIsActiveTrue(Collection<Long> ids);

    List<TravelInsurancePlanEntity> findByFamilyIdAndIsActiveTrue(Long familyId);

    @Query(
            """
            select
                fam.id as familyId,
                fam.familyName as familyName,
                fam.isLoss as isLoss,
                p.id as planId,
                p.ageGroupId as ageGroupId,
                p.planCode as planCode,
                p.planGroupCode as planGroupCode,
                p.productCode as productCode,
                p.unitProductCode as unitProductCode
            from TravelInsurancePlanEntity p
            join TravelInsurancePlanFamilyEntity fam on fam.id = p.familyId
            where p.insurerId = :insurerId
              and p.isActive = true
              and fam.isActive = true
              and fam.deletedAt is null
            order by fam.sortOrder asc, p.ageGroupId asc, p.sortOrder asc, p.id asc
                """)
    List<PlanFamilyPlanRow> findActiveFamilyPlans(@Param("insurerId") Long insurerId);
}
