package com.nexsol.tpa.core.domain.client;

public interface CancelProvider {
    void cancelContract(String company, String polNo, String quotGrpNo, String quotReqNo);
}
