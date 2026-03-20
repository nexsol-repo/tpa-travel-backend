package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.apply.NewInsuredPerson;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsuredRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPeopleWriter {

    private final TravelInsuredRepository insuredRepository;

    public void register(Long contractId, List<NewInsuredPerson> people) {
        IntStream.range(0, people.size())
                .mapToObj(
                        i -> {
                            var p = people.get(i);
                            return TravelInsuredEntity.create(
                                    contractId,
                                    p.planId(),
                                    i == 0,
                                    p.name(),
                                    p.gender(),
                                    p.residentNumber(),
                                    p.englishName(),
                                    p.passportNumber(),
                                    p.phone(),
                                    p.email(),
                                    p.insurePremium());
                        })
                .forEach(insuredRepository::save);
    }
}
