package com.nexsol.tpa.core.domain.subscription;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient.EstimateSaveRequest;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.InsuredPerson;
import com.nexsol.tpa.core.domain.contract.ResidentNumberParser;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.PlanReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionInsuredReader {

    private final ContractPeopleFinder contractPeopleFinder;
    private final PlanReader planReader;

    public List<EstimateSaveRequest.InsuredPerson> findEstimateSaveInsuredPeople(Long contractId) {
        List<InsuredPerson> people = contractPeopleFinder.findByContractId(contractId);

        Map<Long, InsurancePlan> planCache =
                people.stream()
                        .map(InsuredPerson::planId)
                        .filter(id -> id != null)
                        .distinct()
                        .collect(Collectors.toMap(Function.identity(), planReader::getById));

        return people.stream()
                .map(
                        person -> {
                            InsurancePlan plan = planCache.get(person.planId());
                            return new EstimateSaveRequest.InsuredPerson(
                                    ResidentNumberParser.extractBirthYmd(
                                            person.residentNumber()),
                                    ResidentNumberParser.normalizeGenderToMeritz(
                                            person.gender(), person.residentNumber()),
                                    person.name(),
                                    person.englishName(),
                                    plan.planGroupCode(),
                                    plan.planCode());
                        })
                .toList();
    }

    /**
     * 첫번째 피보험자의 plan을 반환한다. (대표 plan — productCode/unitProductCode 용)
     */
    public InsurancePlan findRepPlan(Long contractId) {
        return contractPeopleFinder.findByContractId(contractId).stream()
                .map(InsuredPerson::planId)
                .filter(id -> id != null)
                .findFirst()
                .map(planReader::getById)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "피보험자에 planId가 없습니다. contractId=" + contractId));
    }
}