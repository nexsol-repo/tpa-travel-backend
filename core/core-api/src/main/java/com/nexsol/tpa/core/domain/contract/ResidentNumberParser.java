package com.nexsol.tpa.core.domain.contract;

public final class ResidentNumberParser {

    private ResidentNumberParser() {}

    public static String extractBirthYmd(String rrn) {
        if (rrn == null)
            throw new IllegalStateException("residentNumber is required to build inspeBdt");
        String digits = rrn.replaceAll("[^0-9]", "");
        if (digits.length() < 7) throw new IllegalStateException("invalid residentNumber: " + rrn);

        String yymmdd = digits.substring(0, 6);
        char s = digits.charAt(6);

        String century;
        if (s == '1' || s == '2' || s == '5' || s == '6') century = "19";
        else if (s == '3' || s == '4' || s == '7' || s == '8') century = "20";
        else century = "19";

        return century + yymmdd;
    }

    public static String normalizeGenderToMeritz(String gender, String rrn) {
        if (gender != null) {
            String g = gender.trim().toUpperCase();
            if ("1".equals(g) || "2".equals(g)) return g;
            if ("M".equals(g) || "MALE".equals(g) || "남".equals(g)) return "1";
            if ("F".equals(g) || "FEMALE".equals(g) || "여".equals(g)) return "2";
        }

        String digits = rrn == null ? "" : rrn.replaceAll("[^0-9]", "");
        if (digits.length() >= 7) {
            char s = digits.charAt(6);
            if (s == '1' || s == '3' || s == '5' || s == '7') return "1";
            if (s == '2' || s == '4' || s == '6' || s == '8') return "2";
        }

        throw new IllegalStateException(
                "cannot determine gender. gender=" + gender + ", rrn=" + rrn);
    }
}
