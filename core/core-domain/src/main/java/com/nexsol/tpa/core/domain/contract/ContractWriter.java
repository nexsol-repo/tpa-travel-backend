package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelContractRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractWriter {

    private final TravelContractRepository contractRepository;

    public TravelContractEntity save(TravelContractEntity contract) {
        return contractRepository.save(contract);
    }

    public void updateWithMeritzResult(
            TravelContractEntity contract,
            BigDecimal ttPrem,
            String polNo,
            String quotGrpNo,
            String quotReqNo) {
        contract.updateMeritzResult(ttPrem, polNo, quotGrpNo, quotReqNo);
    }

    public void markCompleted(TravelContractEntity contract) {
        contract.markCompleted();
        contractRepository.save(contract);
    }
}
