package com.nexsol.tpa.core.domain.client;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.subscription.EstimateSaveResult;
import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;

public interface SubscriptionProvider {

    EstimateSaveResult estimateSave(String company, ContractInfo contract, SubscriptionCommand cmd);
}
