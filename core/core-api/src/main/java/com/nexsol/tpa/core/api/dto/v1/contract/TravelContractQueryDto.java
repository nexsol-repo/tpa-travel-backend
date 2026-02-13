package com.nexsol.tpa.core.api.dto.v1.contract;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TravelContractQueryDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractListItem {

        private Long id;

        private Long insurerId;

        private String insurerName;

        private Long partnerId;

        private String partnerName;

        private Long channelId;

        private String channelName;

        private Long planId;

        private String planName;

        private String policyNumber;

        private String countryName;

        private String countryCode;

        private Integer insuredPeopleNumber;

        private BigDecimal totalPremium;

        private String status;

        private String authUniqueKey;

        private String authStatus;

        private LocalDateTime authDate;

        private LocalDateTime applyDate;

        private LocalDateTime insureStartDate;

        private LocalDateTime insureEndDate;

        // 결제 요약(목록에도 보통 필요)
        private Payment payment;

        private List<PersonSummary> people;

        // 계약자 정보
        private String contractPeopleName;

        private String contractPeopleResidentNumberMasked;

        private String contractPeopleHp;

        private String contractPeopleMail;

        private String termsUrl;

        private String policyLink;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonSummary {

        private Long id;

        private String name; // korNm

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractDetail {

        private Contract contract;

        private Insurer insurer;

        private Partner partner;

        private Channel channel;

        private Plan plan;

        private Payment payment;

        private String termsUrl;

        private String policyLink;

        private List<Person> people;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Contract {

        private Long id;

        private Long insurerId;

        private Long partnerId;

        private Long channelId;

        private Long planId;

        private String insurerName;

        private String partnerName;

        private String channelName;

        private String policyNumber;

        private String meritzQuoteGroupNumber;

        private String meritzQuoteRequestNumber;

        private String countryName;

        private String countryCode;

        private Integer insuredPeopleNumber;

        private BigDecimal totalPremium;

        private String policyLink;

        private String status;

        private LocalDateTime applyDate;

        private LocalDateTime insureStartDate;

        private LocalDateTime insureEndDate;

        private String contractPeopleName;

        private String contractPeopleResidentNumberMasked;

        private String contractPeopleHp;

        private String contractPeopleMail;

        private boolean marketingConsentUsed;

        private String authProvider;

        private String authImpUid;

        private String authRequestId;

        private String authUniqueKey;

        private String authStatus;

        private LocalDateTime authDate;

        private Long employeeId;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payment {

        private Long id;

        private String paymentMethod;

        private String status;

        private BigDecimal paidAmount;

        private LocalDateTime paymentDate;

        private LocalDateTime cancelDate;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Person {

        private Long id;

        private String name;

        private String nameEng;

        private String gender;

        private String residentNumberMasked;

        private String passportNumberMasked;

        private String policyNumber;

        private BigDecimal insurePremium;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Insurer {

        private Long id;

        private String insurerCode;

        private String insurerName;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Partner {

        private Long id;

        private String partnerCode;

        private String partnerName;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Channel {

        private Long id;

        private String channelCode;

        private String channelName;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Plan {

        private Long id;

        private String insuranceProductName;

        private String planName;

        private String planFullName;

        private String productCode;

        private String unitProductCode;

        private String planGroupCode;

        private String planCode;

    }

}
