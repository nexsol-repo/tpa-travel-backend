package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.core.api.repository.v1.projection.PlanFamilyPlanRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TravelInsurancePlanRepository extends JpaRepository<TravelInsurancePlanEntity, Long> {

    @Query("""
                select p
                from TravelInsurancePlanEntity p
                where p.insurerId = :insurerId
                  and p.isActive = true
                order by p.sortOrder asc, p.id asc
            """)
    List<TravelInsurancePlanEntity> findActiveByInsurerId(@Param("insurerId") Long insurerId);

    @Query("""
                select p
                from TravelInsurancePlanEntity p
                where p.id in (:ids)
            """)
    List<TravelInsurancePlanEntity> findByIdIn(@Param("ids") Collection<Long> ids);

    List<TravelInsurancePlanEntity> findByInsurerIdAndIsActiveTrueOrderBySortOrderAsc(Long insurerId);

    List<TravelInsurancePlanEntity> findByIdInAndIsActiveTrue(Collection<Long> ids);

    @Query("""
            select
                fam.id as familyId,
                fam.familyName as familyName,
                p.id as planId,
                p.ageGroupId as ageGroupId,
                p.planCode as planCode,
                p.planGroupCode as planGroupCode,
                p.productCode as productCode,
                p.unitProductCode as unitProductCode
            from TravelInsurancePlanFamilyEntity fam
            join TravelInsurancePlanFamilyMapEntity fmap on fmap.familyId = fam.id
            join TravelInsurancePlanEntity p on p.id = fmap.planId
            where fam.insurerId = :insurerId
              and fam.isActive = true
              and fam.deletedAt is null
              and p.isActive = true
            order by fam.sortOrder asc, p.ageGroupId asc, p.sortOrder asc, p.id asc
                """)
    List<PlanFamilyPlanRow> findActiveFamilyPlans(@Param("insurerId") Long insurerId);

}
