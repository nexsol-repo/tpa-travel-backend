package com.nexsol.tpa.core.domain.apply;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.InsuranceConfig;
import com.nexsol.tpa.core.domain.contract.ContractCreator;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractPeopleWriter;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyValidator applyValidator;
    private final ContractCreator contractCreator;
    private final ContractWriter contractWriter;
    private final ContractPeopleWriter peopleWriter;
    private final SnapshotAppender snapshotAppender;
    private final InsuranceConfig companies;
    private final ObjectMapper objectMapper;

    public Long apply(ContractApply cmd) {
        applyValidator.validate(cmd);

        ContractInfo contract = contractCreator.create(cmd, companies.getTpaInfo().polNo());
        ContractInfo saved = contractWriter.writerContract(contract);

        peopleWriter.register(saved.id(), cmd.people());
        snapshotAppender.append(saved.id(), cmd.insurerId(), "QUOTE", toJson(saved.id()));

        return saved.id();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[APPLY] JSON 직렬화 실패", e);
            return "{}";
        }
    }
}
