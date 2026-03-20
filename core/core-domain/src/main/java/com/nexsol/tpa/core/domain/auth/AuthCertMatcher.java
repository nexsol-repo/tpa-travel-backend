package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.portone.PortOneClient;
import com.nexsol.tpa.core.domain.contract.InsuredPerson;

@Component
public class AuthCertMatcher {

    public boolean isMatched(
            InsuredPerson contractor, PortOneClient.CertificationResponse cert) {
        if (contractor == null) return false;

        boolean nameOk =
                contractor.name() != null
                        && cert.getName() != null
                        && normalizeName(contractor.name())
                                .equals(normalizeName(cert.getName()));

        boolean phoneOk =
                contractor.phone() != null
                        && cert.getPhone() != null
                        && normalizePhone(contractor.phone())
                                .equals(normalizePhone(cert.getPhone()));

        return nameOk && phoneOk;
    }

    public String normalizeGender(String gender) {
        if (gender == null) return null;
        String normalized = gender.trim().toUpperCase();
        if (normalized.equals("MALE") || normalized.equals("M") || normalized.equals("1"))
            return "M";
        if (normalized.equals("FEMALE") || normalized.equals("F") || normalized.equals("2"))
            return "F";
        return gender;
    }

    private String normalizeName(String value) {
        return value.replaceAll("\\s+", "");
    }

    private String normalizePhone(String value) {
        return value.replaceAll("[^0-9]", "");
    }
}