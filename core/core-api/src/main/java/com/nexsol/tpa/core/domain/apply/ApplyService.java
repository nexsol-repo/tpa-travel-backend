package com.nexsol.tpa.core.domain.apply;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.core.enums.TravelContractStatus;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurePeopleEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelInsurePeopleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ContractWriter contractWriter;
    private final TravelInsurePeopleRepository peopleRepository;
    private final SnapshotAppender snapshotAppender;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApplyResult apply(ApplyCommand cmd) {
        validate(cmd);

        TravelContractEntity contract =
                TravelContractEntity.builder()
                        .insurerId(cmd.insurerId())
                        .insurerName("MERITZ")
                        .partnerId(cmd.partnerId())
                        .partnerName("TPA KOREA")
                        .channelId(cmd.channelId())
                        .channelName("TPA KOREA")
                        .planId(cmd.planId())
                        .countryName(cmd.countryName())
                        .countryCode(cmd.countryCode())
                        .insureStartDate(cmd.insureBeginDate())
                        .insureEndDate(cmd.insureEndDate())
                        .contractPeopleName(cmd.contractPeopleName())
                        .contractPeopleResidentNumber(cmd.contractPeopleResidentNumber())
                        .contractPeopleHp(cmd.contractPeopleHp())
                        .contractPeopleMail(cmd.contractPeopleMail())
                        .policyNumber(companies.getTpa().getPolNo())
                        .meritzQuoteGroupNumber(cmd.meritzQuoteGroupNumber())
                        .meritzQuoteRequestNumber(cmd.meritzQuoteRequestNumber())
                        .totalPremium(cmd.totalFee())
                        .insuredPeopleNumber(cmd.people().size())
                        .marketingConsentUsed(cmd.marketingConsentUsed())
                        .status(TravelContractStatus.PENDING)
                        .build();

        TravelContractEntity saved = contractWriter.save(contract);

        for (ApplyCommand.InsuredPerson p : cmd.people()) {
            if (p.name() == null || p.name().isBlank()) {
                throw new CoreApiException(
                        CoreApiErrorType.INVALID_CONTRACT_REQUEST, "people.name is required");
            }
            peopleRepository.save(
                    TravelInsurePeopleEntity.builder()
                            .contractId(saved.getId())
                            .planId(p.planId())
                            .name(p.name())
                            .gender(p.gender())
                            .residentNumber(p.residentNumber())
                            .nameEng(p.nameEng())
                            .passportNumber(p.passportNumber())
                            .policyNumber(p.insureNumber())
                            .insurePremium(p.insurePremium())
                            .build());
        }

        ApplyResult result = new ApplyResult(saved.getId(), saved.getStatus().name());

        snapshotAppender.append(saved.getId(), cmd.insurerId(), "QUOTE", toJson(result));

        return result;
    }

    private void validate(ApplyCommand cmd) {
        if (cmd.partnerId() == null || cmd.channelId() == null || cmd.planId() == null) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST,
                    "partnerId/channelId/planId is required");
        }
        if (cmd.insurerId() == null) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST, "insurerCode is required");
        }
        if (cmd.people() == null || cmd.people().isEmpty()) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST, "people is required");
        }
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
