package com.nexsol.tpa.provider.meritz.cancel;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.CancelProvider;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeritzCancelProvider implements CancelProvider {

    private final InsuranceContractClient contractClient;

    @Override
    public void cancelContract(String company, String polNo, String quotGrpNo, String quotReqNo) {
        contractClient.cancelContract(company, polNo, quotGrpNo, quotReqNo);
    }
}
