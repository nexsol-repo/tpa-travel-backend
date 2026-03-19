package com.nexsol.tpa.core.domain.subscription;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient.EstimateSaveRequest;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.ResidentNumberParser;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurePeopleEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionInsuredReader {

    private final ContractPeopleFinder contractPeopleFinder;
    private final PlanReader planReader;

    public List<EstimateSaveRequest.InsuredPerson> findEstimateSaveInsuredPeople(
            Long contractId, TravelInsurancePlanEntity fallbackPlan) {

        List<TravelInsurePeopleEntity> people =
                contractPeopleFinder.findByContractId(contractId);

        // people.planId가 있는 경우 개별 플랜 조회, 없으면 fallbackPlan 사용
        Map<Long, TravelInsurancePlanEntity> planCache =
                people.stream()
                        .map(TravelInsurePeopleEntity::getPlanId)
                        .filter(id -> id != null)
                        .distinct()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                planReader::getById));

        return people.stream()
                .map(person -> {
                    TravelInsurancePlanEntity plan =
                            person.getPlanId() != null
                                    ? planCache.get(person.getPlanId())
                                    : fallbackPlan;
                    return new EstimateSaveRequest.InsuredPerson(
                            ResidentNumberParser.extractBirthYmd(
                                    person.getResidentNumber()),
                            ResidentNumberParser.normalizeGenderToMeritz(
                                    person.getGender(), person.getResidentNumber()),
                            person.getName(),
                            person.getNameEng(),
                            plan.getPlanGroupCode(),
                            plan.getPlanCode());
                })
                .toList();
    }
}
