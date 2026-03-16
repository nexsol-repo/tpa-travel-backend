package com.nexsol.tpa.core.domain.subscription;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient.EstimateSaveRequest;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.ResidentNumberParser;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionInsuredReader {

    private final ContractPeopleFinder contractPeopleFinder;

    public List<EstimateSaveRequest.InsuredPerson> findEstimateSaveInsuredPeople(
            Long contractId, TravelInsurancePlanEntity plan) {
        return contractPeopleFinder.findByContractId(contractId).stream()
                .map(
                        person ->
                                new EstimateSaveRequest.InsuredPerson(
                                        ResidentNumberParser.extractBirthYmd(
                                                person.getResidentNumber()),
                                        ResidentNumberParser.normalizeGenderToMeritz(
                                                person.getGender(), person.getResidentNumber()),
                                        person.getName(),
                                        person.getNameEng(),
                                        plan.getPlanGroupCode(),
                                        plan.getPlanCode()))
                .toList();
    }
}
