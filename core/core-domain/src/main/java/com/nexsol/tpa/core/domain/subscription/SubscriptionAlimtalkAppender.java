package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.alimtalk.AlimtalkCompletedCommand;
import com.nexsol.tpa.core.domain.alimtalk.AlimtalkService;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.InsuredPerson;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionAlimtalkAppender {

    private final AlimtalkService alimtalkService;
    private final ContractPeopleFinder peopleFinder;

    public void appendCompleted(ContractInfo contract) {
        try {
            InsuredPerson contractor = peopleFinder.findContractor(contract.id());
            alimtalkService.sendTravelContractCompleted(
                    new AlimtalkCompletedCommand(
                            contractor != null ? contractor.phone() : null,
                            contractor != null ? contractor.name() : null,
                            "여행자보험",
                            contract.policyNumber(),
                            contract.policyLink(),
                            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf"));
        } catch (Exception ignore) {
        }
    }
}
