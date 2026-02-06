package com.nexsol.tpa.core.api.repository.v1.projection;

public interface TravelPlanCoverageRow {

    Long getPlanId();

    String getCoverageCode();

    String getCoverageName();

    String getDisplayName();

    boolean isIncluded();

    int getSortOrder();

    boolean isMajorCoverage();

    boolean isTitleYn();

    String getCategoryCode();

    // override용 (필요 시)
    String getClaimReasonOverride();

    String getClaimContentOverride();

    String getSubTitleOverride();

    String getSubContentOverride();

}
