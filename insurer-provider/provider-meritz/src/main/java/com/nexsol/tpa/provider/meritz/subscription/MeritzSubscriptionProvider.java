package com.nexsol.tpa.provider.meritz.subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceContractClient;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.EstimateSaveCommand;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.EstimateSaveCommand.InsuredPersonCommand;
import com.nexsol.tpa.core.domain.client.SubscriptionProvider;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.subscription.EstimateSaveResult;
import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;
import com.nexsol.tpa.core.domain.subscription.SubscriptionInsuredReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeritzSubscriptionProvider implements SubscriptionProvider {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InsuranceContractClient contractClient;
    private final SubscriptionInsuredReader subscriptionInsuredReader;

    @Override
    public EstimateSaveResult estimateSave(
            String company, ContractInfo contract, SubscriptionCommand cmd) {

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

        InsuranceContractClient.SubscriptionResult apiResult = contractClient.estimateSave(command);

        if (!apiResult.success()) {
            return EstimateSaveResult.fail(
                    apiResult.errCd(), apiResult.errMsg(), apiResult.rawData());
        }
        return EstimateSaveResult.success(
                apiResult.ttPrem(),
                apiResult.polNo(),
                apiResult.quotGrpNo(),
                apiResult.quotReqNo(),
                apiResult.rawData());
    }
}
