package com.nexsol.tpa.core.api.dto.v1.contract;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class InsurePeopleRequest {
    private String name;
    private String gender;
    private String residentNumber;
    private String nameEng;
    private String passportNumber;
    private String insureNumber;
    private BigDecimal insurePremium;
}
