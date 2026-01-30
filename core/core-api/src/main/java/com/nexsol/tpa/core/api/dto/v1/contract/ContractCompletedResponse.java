package com.nexsol.tpa.core.api.dto.v1.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ContractCompletedResponse {

    private Contract contract;
    private Insurer insurer;
    private Plan plan;

    @Getter
    @AllArgsConstructor
    public static class Contract {
        private Long id;
        private Long partnerId;
        private Long channelId;
        private Long planId;

        private String policyNumber;
        private String meritzQuoteGroupNumber;
        private String meritzQuoteRequestNumber;

        private String countryName;
        private String countryCode;

        private Integer insuredPeopleNumber;
        private BigDecimal totalFee;

        private String status; // PENDING/COMPLETED/ERROR

        private LocalDateTime insureBeginDate;
        private LocalDateTime insureEndDate;

        private String contractPeopleName;
        private String contractPeopleHp;
        private String contractPeopleMail;
    }

    @Getter
    @AllArgsConstructor
    public static class Insurer {
        private Long id;
        private String name; // 예: "메리츠화재"
        private String code; // 있으면(없으면 null)
    }

    @Getter
    @AllArgsConstructor
    public static class Plan {
        private Long id;
        private String insuranceProductName; // 예: "해외여행보험"
        private String planName;             // 예: "만편한 플랜"

        // 메리츠 매핑용 코드들
        private String productCode;      // pdCd
        private String unitProductCode;  // untPdCd
        private String planGroupCode;    // planGrpCd
        private String planCode;         // planCd
    }
}
