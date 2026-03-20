package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexsol.tpa.storage.db.core.entity.TravelPlanCoverageEntity;
import com.nexsol.tpa.storage.db.core.repository.projection.TravelPlanCoverageRow;

public interface JpaPlanCoverageRepository
        extends JpaRepository<TravelPlanCoverageEntity, Long> {

    @Query(
            """
                select
                    pc.planId as planId,
                    c.coverageCode as coverageCode,
                    c.coverageName as coverageName,
                    pc.displayName as displayName,
                    pc.isIncluded as included,
                    pc.sortOrder as sortOrder,
                    pc.isMajorCoverage as majorCoverage,
                    pc.titleYn as titleYn,
                    pc.categoryCode as categoryCode,
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
