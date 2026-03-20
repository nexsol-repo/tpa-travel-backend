package com.nexsol.tpa.core.domain.repository.projection;

public interface PlanFamilyPlanRow {

    Long getFamilyId();

    String getFamilyName();

    Long getPlanId();

    Integer getAgeGroupId();

    String getPlanCode();

    String getPlanGroupCode();

    String getProductCode();

    String getUnitProductCode();

    Boolean getIsLoss();
}