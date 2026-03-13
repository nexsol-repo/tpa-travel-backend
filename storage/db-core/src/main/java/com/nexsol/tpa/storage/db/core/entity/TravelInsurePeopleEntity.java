package com.nexsol.tpa.storage.db.core.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "travel_insure_people")
public class TravelInsurePeopleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

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
