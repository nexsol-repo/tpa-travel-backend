package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsuredPremiumResponse {

    private Integer index;

    private Long planId;

    private String currency;

    private Long ppsPrem;

    private String birth;

    private String gndrCd;

    private String cusNm;

    private String cusEngNm;

    private String ageBandCode;

    private String ageBandLabel;
}
