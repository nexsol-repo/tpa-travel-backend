package com.nexsol.tpa.core.api.dto.v1.contract;

import com.nexsol.tpa.core.api.dto.v1.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ContractCompletedResponse extends BaseResponse {

    private final Contract contract;

    private final Insurer insurer;

    private final Plan plan;

    // =========================
    // 성공 생성자
    // =========================
    public ContractCompletedResponse(Contract contract, Insurer insurer, Plan plan) {
        super(true, "00001", "0");
        this.contract = contract;
        this.insurer = insurer;
        this.plan = plan;
    }

    // =========================
    // 실패 생성자
    // =========================
    public ContractCompletedResponse(String errCd, String errMsg) {
        super(false, errCd, errMsg);
        this.contract = null;
        this.insurer = null;
        this.plan = null;
    }

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

        private String name;

        private String code;

    }

    @Getter
    @AllArgsConstructor
    public static class Plan {

        private Long id;

        private String insuranceProductName;

        private String planName;

        private String productCode;

        private String unitProductCode;

        private String planGroupCode;

        private String planCode;

    }

}
