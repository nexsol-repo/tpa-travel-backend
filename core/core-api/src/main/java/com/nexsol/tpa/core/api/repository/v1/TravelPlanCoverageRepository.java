package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelPlanCoverageEntity;
import com.nexsol.tpa.core.api.repository.v1.projection.TravelPlanCoverageRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TravelPlanCoverageRepository
        extends JpaRepository<TravelPlanCoverageEntity, Long> {

    @Query("""
        select
            pc.planId as planId,
            c.coverageCode as coverageCode,
            c.coverageName as coverageName,
            pc.displayName as displayName,
            pc.isIncluded as included,
            pc.sortOrder as sortOrder,
            pc.isMajorCoverage as majorCoverage,
            pc.claimReasonOverride as claimReasonOverride,
            pc.claimContentOverride as claimContentOverride,
            pc.subTitleOverride as subTitleOverride,
            pc.subContentOverride as subContentOverride
        from TravelPlanCoverageEntity pc
        join TravelInsuranceCoverageEntity c on c.id = pc.coverageId
        where pc.planId = :planId
        order by pc.sortOrder asc, pc.id asc
    """)
    List<TravelPlanCoverageRow> findRowsByPlanId(@Param("planId") Long planId);
}
