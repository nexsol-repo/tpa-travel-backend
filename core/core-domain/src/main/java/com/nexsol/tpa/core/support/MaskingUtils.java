package com.nexsol.tpa.core.support;

public final class MaskingUtils {

    private MaskingUtils() {}

    public static String maskRrn(String rrn) {
        if (rrn == null || rrn.isBlank()) return null;
        String digits = rrn.replaceAll("[^0-9]", "");
        if (digits.length() < 6) return "******-*******";
        return digits.substring(0, 6) + "-*******";
    }

    public static String maskPassport(String passport) {
        if (passport == null || passport.isBlank()) return null;
        if (passport.length() <= 3) return "***";
        return passport.substring(0, 2) + "***" + passport.substring(passport.length() - 1);
    }
}
