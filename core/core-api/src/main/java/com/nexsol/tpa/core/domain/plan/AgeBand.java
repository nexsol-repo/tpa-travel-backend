package com.nexsol.tpa.core.domain.plan;

/**
 * 연령대 구분 (도메인 타입).
 * 연령 범위 정의를 한 곳에서 관리한다.
 */
public enum AgeBand {
    AGE_0_14("AGE_0_14", "0~14세", 0, 14, 1),
    AGE_15_69("AGE_15_69", "15~69세", 15, 69, 2),
    AGE_70_80("AGE_70_80", "70~80세", 70, 80, 3);

    private final String code;
    private final String label;
    private final int min;
    private final int max;
    private final int ageGroupId;

    AgeBand(String code, String label, int min, int max, int ageGroupId) {
        this.code = code;
        this.label = label;
        this.min = min;
        this.max = max;
        this.ageGroupId = ageGroupId;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public int min() {
        return min;
    }

    public int ageGroupId() {
        return ageGroupId;
    }

    public static AgeBand fromAge(int age) {
        for (AgeBand b : values()) {
            if (age >= b.min && age <= b.max) return b;
        }
        return null;
    }

    public static AgeBand fromCode(String code) {
        for (AgeBand b : values()) {
            if (b.code.equals(code)) return b;
        }
        return null;
    }
}
