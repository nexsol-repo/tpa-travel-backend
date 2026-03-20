package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.apply.NewInsuredPerson;
import com.nexsol.tpa.core.domain.repository.InsuredRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPeopleWriter {

    private final InsuredRepository insuredRepository;

    public void register(Long contractId, List<NewInsuredPerson> people) {
        IntStream.range(0, people.size())
                .mapToObj(
                        i -> {
                            var p = people.get(i);
                            return InsuredPerson.builder()
                                    .contractId(contractId)
                                    .planId(p.planId())
                                    .isContractor(i == 0)
                                    .name(p.name())
                                    .gender(p.gender())
                                    .residentNumber(p.residentNumber())
                                    .englishName(p.englishName())
                                    .passportNumber(p.passportNumber())
                                    .phone(p.phone())
                                    .email(p.email())
                                    .insurePremium(p.insurePremium())
                                    .build();
                        })
                .forEach(insuredRepository::save);
    }
}