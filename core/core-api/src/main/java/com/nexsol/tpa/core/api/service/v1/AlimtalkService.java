package com.nexsol.tpa.core.api.service.v1;

import com.nexsol.tpa.core.api.dto.v1.TravelAlimtalkCompletedCommand;

public interface AlimtalkService {
    void sendTravelContractCompleted(TravelAlimtalkCompletedCommand cmd);
}
