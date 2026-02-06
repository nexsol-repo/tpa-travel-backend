package com.nexsol.tpa.core.api.dto.v1.contract;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ContractApplyRequest {

    private Long insurerId;

    private String insuerName;

    private Long partnerId;

    private String partnerName;

    private Long channelId;

    private String channelName;

    private Long planId;

    private String policyNumber;

    private String meritzQuoteGroupNumber;

    private String meritzQuoteRequestNumber;

    private String countryCode;

    private String countryName;

    private LocalDateTime insureBeginDate;

    private LocalDateTime insureEndDate;

    private String contractPeopleName;

    private String contractPeopleResidentNumber;

    private String contractPeopleHp;

    private String contractPeopleMail;

    private BigDecimal totalFee;

    private List<InsurePeopleRequest> people;

    private boolean marketingConsentUsed;

}
