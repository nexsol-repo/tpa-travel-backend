package com.nexsol.tpa.core.domain.subscription;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.InsuredPerson;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionResultReader {

    private final SubscriptionInsuredReader subscriptionInsuredReader;
    private final ContractPeopleFinder peopleFinder;

    public SubscriptionResult read(ContractInfo contract) {
        InsurancePlan plan = subscriptionInsuredReader.findRepPlan(contract.id());
        List<InsuredPerson> people = peopleFinder.findByContractId(contract.id());
        InsuredPerson contractor = peopleFinder.findContractor(contract.id());

        return SubscriptionResult.success(
                contract.id(),
                plan.insuranceProductName(),
                plan.planName(),
                contract.insurePeriod().startDate(),
                contract.insurePeriod().endDate(),
                contractor != null ? contractor.name() : null,
                people.size());
    }
}