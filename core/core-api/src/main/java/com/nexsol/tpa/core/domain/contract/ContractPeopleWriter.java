package com.nexsol.tpa.core.domain.contract;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.apply.ApplyCommand;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsuredRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPeopleWriter {

    private final TravelInsuredRepository insuredRepository;

    public void saveAll(Long contractId, List<ApplyCommand.InsuredPerson> people) {
        boolean first = true;
        for (ApplyCommand.InsuredPerson p : people) {
            insuredRepository.save(
                    TravelInsuredEntity.builder()
                            .contractId(contractId)
                            .planId(p.planId())
                            .isContractor(first)
                            .name(p.name())
                            .gender(p.gender())
                            .residentNumber(p.residentNumber())
                            .englishName(p.englishName())
                            .passportNumber(p.passportNumber())
                            .phone(p.phone())
                            .email(p.email())
                            .policyNumber(p.insureNumber())
                            .insurePremium(p.insurePremium())
                            .build());
            first = false;
        }
    }
}