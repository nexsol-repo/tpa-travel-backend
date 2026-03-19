package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.alimtalk.AlimtalkCompletedCommand;
import com.nexsol.tpa.core.domain.alimtalk.AlimtalkService;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsuredEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionAlimtalkAppender {

    private final AlimtalkService alimtalkService;
    private final ContractPeopleFinder peopleFinder;

    public void appendCompleted(TravelContractEntity contract) {
        try {
            TravelInsuredEntity contractor = peopleFinder.findContractor(contract.getId());
            alimtalkService.sendTravelContractCompleted(
                    new AlimtalkCompletedCommand(
                            contractor != null ? contractor.getPhone() : null,
                            contractor != null ? contractor.getName() : null,
                            "여행자보험",
                            contract.getPolicyNumber(),
                            contract.getPolicyLink(),
                            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf"));
        } catch (Exception ignore) {
        }
    }
}
