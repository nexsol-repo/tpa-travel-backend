package com.nexsol.tpa.core.api.entity;

import com.nexsol.tpa.core.api.dto.v1.contract.TravelContractStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "travel_contract")
public class TravelContractEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "insurer_name")
    private String insurerName;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "meritz_quote_group_number")
    private String meritzQuoteGroupNumber;

    @Column(name = "meritz_quote_request_number")
    private String meritzQuoteRequestNumber;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "insured_people_number", nullable = false)
    private Integer insuredPeopleNumber = 1;

    @Column(name = "total_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPremium = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TravelContractStatus status = TravelContractStatus.PENDING;

    @Column(name = "apply_date", insertable = false, updatable = false)
    private LocalDateTime applyDate;

    @Column(name = "insure_start_date")
    private LocalDateTime insureStartDate;

    @Column(name = "insure_end_date")
    private LocalDateTime insureEndDate;

    @Column(name = "contract_people_name")
    private String contractPeopleName;

    @Column(name = "contract_people_resident_number")
    private String contractPeopleResidentNumber; // TODO 암호화

    @Column(name = "contract_people_hp")
    private String contractPeopleHp;

    @Column(name = "contract_people_mail")
    private String contractPeopleMail;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "marketing_consent_used", nullable = false)
    private Boolean marketingConsentUsed = false;

    @OneToMany(
            mappedBy = "contract",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TravelInsurePeopleEntity> people = new ArrayList<>();

    public void addPerson(TravelInsurePeopleEntity person) {
        person.setContract(this);
        this.people.add(person);
    }

    public void markCompleted() {
        this.status = TravelContractStatus.COMPLETED;
        markUpdated(null);
    }

    public void markError() {
        this.status = TravelContractStatus.ERROR;
        markUpdated(null);
    }

    public static TravelContractEntity createPending() {
        TravelContractEntity c = new TravelContractEntity();
        c.status = TravelContractStatus.PENDING;
        c.applyDate = LocalDateTime.now();
        return c;
    }
}
