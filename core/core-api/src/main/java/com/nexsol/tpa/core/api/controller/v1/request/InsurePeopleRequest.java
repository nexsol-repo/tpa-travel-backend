package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InsurePeopleRequest {

    private final String name;
    private final String gender;
    private final String residentNumber;
    private final String nameEng;
    private final String passportNumber;
    private final String insureNumber;
    private final BigDecimal insurePremium;

    @Builder
    private InsurePeopleRequest(
            String name,
            String gender,
            String residentNumber,
            String nameEng,
            String passportNumber,
            String insureNumber,
            BigDecimal insurePremium) {
        this.name = name;
        this.gender = gender;
        this.residentNumber = residentNumber;
        this.nameEng = nameEng;
        this.passportNumber = passportNumber;
        this.insureNumber = insureNumber;
        this.insurePremium = insurePremium;
    }
}
