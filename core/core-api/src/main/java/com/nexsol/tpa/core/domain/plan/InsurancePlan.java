package com.nexsol.tpa.core.domain.plan;

import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

import lombok.Builder;

@Builder
public record InsurancePlan(
        Long id,
        String insuranceProductName,
        String planName,
        String planFullName,
        String productCode,
        String unitProductCode,
        String planGroupCode,
        String planCode) {

    public static InsurancePlan of(TravelInsurancePlanEntity e) {
        if (e == null) return null;
        return InsurancePlan.builder()
                .id(e.getId())
                .insuranceProductName(e.getInsuranceProductName())
                .planName(toDisplayName(e.getPlanFullName(), e.getPlanName()))
                .planFullName(e.getPlanFullName())
                .productCode(e.getProductCode())
                .unitProductCode(e.getUnitProductCode())
                .planGroupCode(e.getPlanGroupCode())
                .planCode(e.getPlanCode())
                .build();
    }

    private static String toDisplayName(String planFullName, String planName) {
        String name = planFullName != null ? planFullName : planName;
        if (name == null) return null;
        return name.replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .replaceAll("_\\d+~\\d+세$", "")
                .trim();
    }
}
