package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionResultReader {

    private final PlanReader planReader;

    public SubscriptionResult read(TravelContractEntity contract) {
        TravelInsurancePlanEntity plan = planReader.getById(contract.getPlanId());
        TravelInsurerEntity insurer = planReader.getInsurerById(plan.getInsurerId());

        return SubscriptionResult.success(
                new SubscriptionResult.ContractInfo(
                        contract.getId(),
                        contract.getPartnerId(),
                        contract.getChannelId(),
                        contract.getPlanId(),
                        contract.getFamilyId(),
                        contract.getPolicyNumber(),
                        contract.getMeritzQuoteGroupNumber(),
                        contract.getMeritzQuoteRequestNumber(),
                        contract.getCountryName(),
                        contract.getCountryCode(),
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
