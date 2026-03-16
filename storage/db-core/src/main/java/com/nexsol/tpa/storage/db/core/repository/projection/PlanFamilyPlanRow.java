package com.nexsol.tpa.storage.db.core.repository.projection;

public interface PlanFamilyPlanRow {

    Long getFamilyId();

    String getFamilyName();

    Long getPlanId();

    Integer getAgeGroupId(); // plan.age_group_id

    String getPlanCode(); // plan.plan_code

    String getPlanGroupCode(); // plan.plan_group_code

    String getProductCode(); // plan.product_code (pdCd)

    String getUnitProductCode(); // plan.unit_product_code (untPdCd)
}
