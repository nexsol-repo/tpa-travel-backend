package com.nexsol.tpa.core.api.controller.v1.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsurePeopleRequest {

    private String name;

    private String gender;

    private String residentNumber;

    private String nameEng;

    private String passportNumber;

    private String insureNumber;

    private BigDecimal insurePremium;
}
