package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "travel_insure_people")
public class TravelInsurePeopleEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: travel_insure_people.contract_id -> travel_contract.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private TravelContractEntity contract;

    @Column(name = "insure_people_name", length = 50)
    private String name;

    @Column(name = "insure_people_gender", length = 10)
    private String gender;

    @Column(name = "insure_people_resident_number", length = 20)
    private String residentNumber;

    @Column(name = "insure_people_name_eng", length = 100)
    private String nameEng;

    @Column(name = "insure_people_passport_number", length = 30)
    private String passportNumber;

    @Column(name = "policy_number", length = 50)
    private String policyNumber;

    @Column(name = "insure_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal insurePremium = BigDecimal.ZERO;

}
