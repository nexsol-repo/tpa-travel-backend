package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.alimtalk.AlimtalkCompletedCommand;
import com.nexsol.tpa.core.domain.alimtalk.AlimtalkService;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionAlimtalkAppender {

    private final AlimtalkService alimtalkService;

    public void appendCompleted(TravelContractEntity contract) {
        try {
            alimtalkService.sendTravelContractCompleted(
                    new AlimtalkCompletedCommand(
                            contract.getContractPeopleHp(),
                            contract.getContractPeopleName(),
                            "여행자보험",
                            contract.getPolicyNumber(),
                            contract.getPolicyLink(),
                            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf"));
        } catch (Exception ignore) {
        }
    }
}
