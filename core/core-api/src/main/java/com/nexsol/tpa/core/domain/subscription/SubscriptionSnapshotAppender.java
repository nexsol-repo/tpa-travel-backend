package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSnapshotAppender {

    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;

    public void appendFail(TravelContractEntity contract, Object rawData) {
        snapshotAppender.append(
                contract.getId(), contract.getInsurerId(), "PAYMENT_FAIL", toJson(rawData));
    }

    public void appendSuccess(TravelContractEntity contract, Object rawData) {
        snapshotAppender.append(
                contract.getId(), contract.getInsurerId(), "PAYMENT", toJson(rawData));
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
