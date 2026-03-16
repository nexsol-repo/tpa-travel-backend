package com.nexsol.tpa.core.domain.subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient.EstimateSaveRequest;
import com.nexsol.tpa.client.meritz.contract.SubscriptionApiResult;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionEstimateSaver {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MeritzContractClient contractClient;
    private final PlanReader planReader;
    private final SubscriptionInsuredReader subscriptionInsuredReader;

    public SubscriptionApiResult save(
            String company, TravelContractEntity contract, SubscriptionCommand cmd) {

        TravelInsurancePlanEntity plan = planReader.getById(contract.getPlanId());
        String sbcpDt = LocalDate.now().format(YYYYMMDD);

        List<EstimateSaveRequest.InsuredPerson> insuredPeople =
                subscriptionInsuredReader.findEstimateSaveInsuredPeople(contract.getId(), plan);

        EstimateSaveRequest req =
                new EstimateSaveRequest(
                        company,
                        contract.getPolicyNumber(),
                        plan.getProductCode(),
                        plan.getUnitProductCode(),
                        sbcpDt,
                        contract.getInsureStartDate().format(YYYYMMDD),
                        contract.getInsureEndDate().format(YYYYMMDD),
                        contract.getCountryCode(),
                        null,
                        cmd.cardNo(),
                        cmd.efctPrd(),
                        cmd.dporNm(),
                        cmd.dporCd(),
                        insuredPeople);

        return contractClient.estimateSave(req);
    }
}
