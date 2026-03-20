package com.nexsol.tpa.core.domain.apply;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.core.domain.contract.ContractPeopleWriter;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyValidator applyValidator;
    private final ContractWriter contractWriter;
    private final ContractPeopleWriter peopleWriter;
    private final SnapshotAppender snapshotAppender;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long apply(ContractApply cmd) {
        applyValidator.validate(cmd);

        TravelContractEntity contract =
                TravelContractEntity.create(
                        cmd.insurerId(),
                        "MERITZ",
                        cmd.partnerId(),
                        "TPA KOREA",
                        cmd.channelId(),
                        "TPA KOREA",
                        cmd.familyId());

        contract.applyInsurePeriod(
                cmd.insureBeginDate(), cmd.insureEndDate(),
                cmd.countryCode(), cmd.countryName());
        contract.applyMeritzQuote(
                companies.getTpa().getPolNo(),
                cmd.meritzQuoteGroupNumber(),
                cmd.meritzQuoteRequestNumber());
        contract.applyPremium(cmd.totalPremium());
        contract.applyMarketingConsent(cmd.marketingConsentUsed());

        TravelContractEntity saved = contractWriter.save(contract);
        peopleWriter.register(saved.getId(), cmd.people());
        snapshotAppender.append(saved.getId(), cmd.insurerId(), "QUOTE", toJson(saved.getId()));

        return saved.getId();
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
