package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;

@Component
public class SubscriptionResultReader {

    public SubscriptionResult read(
            TravelContractEntity contract,
            TravelInsurancePlanEntity plan,
            TravelInsurerEntity insurer) {
        return SubscriptionResult.success(
                new SubscriptionResult.ContractInfo(
                        contract.getId(),
                        contract.getPartnerId(),
                        contract.getChannelId(),
                        contract.getPlanId(),
                        contract.getPolicyNumber(),
                        contract.getMeritzQuoteGroupNumber(),
                        contract.getMeritzQuoteRequestNumber(),
                        contract.getCountryName(),
                        contract.getCountryCode(),
                        contract.getInsuredPeopleNumber(),
                        contract.getTotalPremium(),
                        contract.getStatus().name(),
                        contract.getInsureStartDate(),
                        contract.getInsureEndDate(),
                        contract.getContractPeopleName(),
                        contract.getContractPeopleHp(),
                        contract.getContractPeopleMail()),
                new SubscriptionResult.InsurerInfo(
                        insurer.getId(), insurer.getInsurerName(), insurer.getInsurerCode()),
                new SubscriptionResult.PlanInfo(
                        plan.getId(),
                        plan.getInsuranceProductName(),
                        plan.getPlanName(),
                        plan.getProductCode(),
                        plan.getUnitProductCode(),
                        plan.getPlanGroupCode(),
                        plan.getPlanCode()));
    }
}
