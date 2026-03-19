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
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionInsuredReader {

    private final ContractPeopleFinder contractPeopleFinder;
    private final PlanReader planReader;

    public List<EstimateSaveRequest.InsuredPerson> findEstimateSaveInsuredPeople(Long contractId) {
        List<TravelInsuredEntity> people = contractPeopleFinder.findByContractId(contractId);

        Map<Long, TravelInsurancePlanEntity> planCache =
                people.stream()
                        .map(TravelInsuredEntity::getPlanId)
                        .filter(id -> id != null)
                        .distinct()
                        .collect(Collectors.toMap(Function.identity(), planReader::getById));

        return people.stream()
                .map(
                        person -> {
                            TravelInsurancePlanEntity plan = planCache.get(person.getPlanId());
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

    /**
     * 첫번째 피보험자의 plan을 반환한다. (대표 plan — productCode/unitProductCode 용)
     */
    public TravelInsurancePlanEntity findRepPlan(Long contractId) {
        return contractPeopleFinder.findByContractId(contractId).stream()
                .map(TravelInsuredEntity::getPlanId)
                .filter(id -> id != null)
                .findFirst()
                .map(planReader::getById)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "피보험자에 planId가 없습니다. contractId=" + contractId));
    }
}
