package com.nexsol.tpa.core.domain.alimtalk;

public interface AlimtalkSender {

    void sendTravelContractCompleted(AlimtalkCompletedCommand cmd);
}
