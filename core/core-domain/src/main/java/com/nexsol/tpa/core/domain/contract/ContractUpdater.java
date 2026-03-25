package com.nexsol.tpa.core.domain.contract;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class ContractUpdater {

    public ContractInfo markCompleted(ContractInfo contract) {
        return ContractInfo.builder()
                .id(contract.id())
                .insurerId(contract.insurerId())
                .insurerName(contract.insurerName())
                .partnerId(contract.partnerId())
                .partnerName(contract.partnerName())
                .channelId(contract.channelId())
                .channelName(contract.channelName())
                .familyId(contract.familyId())
                .policyNumber(contract.policyNumber())
                .quote(contract.quote())
                .totalPremium(contract.totalPremium())
                .policyLink(contract.policyLink())
                .status("COMPLETED")
                .applyDate(contract.applyDate())
                .insurePeriod(contract.insurePeriod())
                .auth(contract.auth())
                .marketingConsentUsed(contract.marketingConsentUsed())
                .employeeId(contract.employeeId())
                .build();
    }

    public ContractInfo markError(ContractInfo contract) {
        return copyWith(contract).status("ERROR").build();
    }

    public ContractInfo updateMeritzResult(
            ContractInfo contract,
            BigDecimal ttPrem,
            String polNo,
            String quotGrpNo,
            String quotReqNo) {
        var builder = copyWith(contract);
        if (ttPrem != null) builder.totalPremium(ttPrem);
        if (polNo != null && !polNo.isBlank()) builder.policyNumber(polNo);
        if (quotGrpNo != null || quotReqNo != null) {
            builder.quote(
                    Quote.builder()
                            .groupNumber(
                                    quotGrpNo != null && !quotGrpNo.isBlank()
                                            ? quotGrpNo
                                            : contract.quote() != null
                                                    ? contract.quote().groupNumber()
                                                    : null)
                            .requestNumber(
                                    quotReqNo != null && !quotReqNo.isBlank()
                                            ? quotReqNo
                                            : contract.quote() != null
                                                    ? contract.quote().requestNumber()
                                                    : null)
                            .build());
        }
        return builder.build();
    }

    public ContractInfo updatePolicyLink(ContractInfo contract, String policyLink) {
        if (policyLink == null || policyLink.isBlank()) return contract;
        return copyWith(contract).policyLink(policyLink).build();
    }

    public ContractInfo updateAuth(
            ContractInfo contract,
            String provider,
            String impUid,
            String requestId,
            String uniqueKey,
            String status) {
        return copyWith(contract)
                .auth(
                        AuthInfo.builder()
                                .provider(provider)
                                .impUid(impUid)
                                .requestId(requestId)
                                .uniqueKey(uniqueKey)
                                .status(status)
                                .build())
                .build();
    }

    private ContractInfo.ContractInfoBuilder copyWith(ContractInfo c) {
        return ContractInfo.builder()
                .id(c.id())
                .insurerId(c.insurerId())
                .insurerName(c.insurerName())
                .partnerId(c.partnerId())
                .partnerName(c.partnerName())
                .channelId(c.channelId())
                .channelName(c.channelName())
                .familyId(c.familyId())
                .policyNumber(c.policyNumber())
                .quote(c.quote())
                .totalPremium(c.totalPremium())
                .policyLink(c.policyLink())
                .status(c.status())
                .applyDate(c.applyDate())
                .insurePeriod(c.insurePeriod())
                .auth(c.auth())
                .marketingConsentUsed(c.marketingConsentUsed())
                .employeeId(c.employeeId());
    }
}
