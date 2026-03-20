package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSnapshotAppender {

    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;

    public void appendFail(ContractInfo contract, Object rawData) {
        snapshotAppender.append(
                contract.id(), contract.insurerId(), "PAYMENT_FAIL", toJson(rawData));
    }

    public void appendSuccess(ContractInfo contract, Object rawData) {
        snapshotAppender.append(
                contract.id(), contract.insurerId(), "PAYMENT", toJson(rawData));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[SUBSCRIPTION] JSON 직렬화 실패", e);
            return "{}";
        }
    }
}