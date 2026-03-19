package com.nexsol.tpa.core.domain.subscription;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionResultReader {

    private final SubscriptionInsuredReader subscriptionInsuredReader;
    private final ContractPeopleFinder peopleFinder;

    public SubscriptionResult read(TravelContractEntity contract) {
        TravelInsurancePlanEntity plan = subscriptionInsuredReader.findRepPlan(contract.getId());
        List<TravelInsuredEntity> people = peopleFinder.findByContractId(contract.getId());

        return SubscriptionResult.success(
                contract.getId(),
                plan.getInsuranceProductName(),
                plan.getPlanName(),
                contract.getInsureStartDate(),
                contract.getInsureEndDate(),
                contract.getContractPeopleName(),
                people.size());
    }
}
