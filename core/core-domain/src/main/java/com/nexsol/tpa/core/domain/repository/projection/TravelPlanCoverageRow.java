package com.nexsol.tpa.core.domain.repository.projection;

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

    String getClaimReasonOverride();

    String getClaimContentOverride();

    String getSubTitleOverride();

    String getSubContentOverride();
}