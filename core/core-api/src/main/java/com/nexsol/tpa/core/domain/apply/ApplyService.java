package com.nexsol.tpa.core.domain.apply;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
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
    public Long apply(ApplyCommand cmd) {
        validate(cmd);

        Long repPlanId = cmd.people().stream()
                .map(ApplyCommand.InsuredPerson::planId)
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);

        TravelContractEntity contract = TravelContractEntity.create(
                cmd.insurerId(), "MERITZ",
                cmd.partnerId(), "TPA KOREA",
                cmd.channelId(), "TPA KOREA",
                repPlanId, cmd.familyId());

        contract.applyInsurePeriod(
                cmd.insureBeginDate(), cmd.insureEndDate(),
                cmd.countryCode(), cmd.countryName());
        contract.applyContractPeople(
                cmd.contractPeopleName(), cmd.contractPeopleResidentNumber(),
                cmd.contractPeopleHp(), cmd.contractPeopleMail());
        contract.applyMeritzQuote(
                companies.getTpa().getPolNo(),
                cmd.meritzQuoteGroupNumber(), cmd.meritzQuoteRequestNumber());
        contract.applyPremium(cmd.totalFee());
        contract.applyMarketingConsent(cmd.marketingConsentUsed());

        TravelContractEntity saved = contractWriter.save(contract);

        boolean first = true;
        for (ApplyCommand.InsuredPerson p : cmd.people()) {
            if (p.name() == null || p.name().isBlank()) {
                throw new CoreApiException(
                        CoreApiErrorType.INVALID_CONTRACT_REQUEST, "people.name is required");
            }
            peopleRepository.save(
                    TravelInsurePeopleEntity.builder()
                            .contractId(saved.getId())
                            .planId(p.planId())
                            .isContractor(first)
                            .name(p.name())
                            .gender(p.gender())
                            .residentNumber(p.residentNumber())
                            .nameEng(p.nameEng())
                            .passportNumber(p.passportNumber())
                            .policyNumber(p.insureNumber())
                            .insurePremium(p.insurePremium())
                            .build());
            first = false;
        }

        snapshotAppender.append(saved.getId(), cmd.insurerId(), "QUOTE", toJson(saved.getId()));

        return saved.getId();
    }

    private void validate(ApplyCommand cmd) {
        if (cmd.partnerId() == null || cmd.channelId() == null || cmd.familyId() == null) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST,
                    "partnerId/channelId/familyId is required");
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
