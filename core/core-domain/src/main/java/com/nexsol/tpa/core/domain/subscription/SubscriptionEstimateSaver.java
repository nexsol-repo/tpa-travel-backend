package com.nexsol.tpa.core.domain.subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceContractClient;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.EstimateSaveCommand;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.EstimateSaveCommand.InsuredPersonCommand;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.SubscriptionResult;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionEstimateSaver {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InsuranceContractClient contractClient;
    private final SubscriptionInsuredReader subscriptionInsuredReader;

    public SubscriptionResult save(String company, ContractInfo contract, SubscriptionCommand cmd) {

        InsurancePlan repPlan = subscriptionInsuredReader.findRepPlan(contract.id());
        String sbcpDt = LocalDate.now().format(YYYYMMDD);

        List<InsuredPersonCommand> insuredPeople =
                subscriptionInsuredReader.findEstimateSaveInsuredPeople(contract.id());

        EstimateSaveCommand command =
                new EstimateSaveCommand(
                        company,
                        contract.policyNumber(),
                        repPlan.productCode(),
                        repPlan.unitProductCode(),
                        sbcpDt,
                        contract.insurePeriod().startDate().format(YYYYMMDD),
                        contract.insurePeriod().endDate().format(YYYYMMDD),
                        contract.insurePeriod().countryCode(),
                        null,
                        cmd.cardNo(),
                        cmd.efctPrd(),
                        cmd.dporNm(),
                        cmd.dporCd(),
                        insuredPeople);

        return contractClient.estimateSave(command);
    }
}
