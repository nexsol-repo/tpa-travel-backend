package com.nexsol.tpa.core.domain.subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient.EstimateSaveRequest;
import com.nexsol.tpa.client.meritz.contract.SubscriptionApiResult;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionEstimateSaver {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MeritzContractClient contractClient;
    private final SubscriptionInsuredReader subscriptionInsuredReader;

    public SubscriptionApiResult save(
            String company, ContractInfo contract, SubscriptionCommand cmd) {

        InsurancePlan repPlan = subscriptionInsuredReader.findRepPlan(contract.id());
        String sbcpDt = LocalDate.now().format(YYYYMMDD);

        List<EstimateSaveRequest.InsuredPerson> insuredPeople =
                subscriptionInsuredReader.findEstimateSaveInsuredPeople(contract.id());

        EstimateSaveRequest req =
                new EstimateSaveRequest(
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

        return contractClient.estimateSave(req);
    }
}