package com.nexsol.tpa.core.domain.apply;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;

@Component
public class ApplyValidator {

    public void validate(ApplyCommand cmd) {
        requireNotNull(cmd.insurerId(), "insurerId");
        requireNotNull(cmd.partnerId(), "partnerId");
        requireNotNull(cmd.channelId(), "channelId");
        requireNotNull(cmd.familyId(), "familyId");
        requireNotNull(cmd.insureBeginDate(), "insureBeginDate");
        requireNotNull(cmd.insureEndDate(), "insureEndDate");
        requireNotBlank(cmd.countryCode(), "countryCode");
        requireNotNull(cmd.totalPremium(), "totalPremium");

        if (cmd.people() == null || cmd.people().isEmpty()) {
            throw invalid("people is required");
        }

        for (int i = 0; i < cmd.people().size(); i++) {
            validatePerson(cmd.people().get(i), i);
        }
    }

    private void validatePerson(ApplyCommand.InsuredPerson p, int index) {
        requireNotNull(p.planId(), "people[" + index + "].planId");
        requireNotBlank(p.name(), "people[" + index + "].name");
        requireNotBlank(p.residentNumber(), "people[" + index + "].residentNumber");
    }

    private void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw invalid(fieldName + " is required");
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw invalid(fieldName + " is required");
        }
    }

    private CoreApiException invalid(String message) {
        return new CoreApiException(CoreApiErrorType.INVALID_CONTRACT_REQUEST, message);
    }
}
