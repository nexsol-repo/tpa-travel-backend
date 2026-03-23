package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.core.domain.contract.*;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;

import lombok.Builder;

@Builder
public record ContractListItem(
        Long id,
        String policyNumber,
        BigDecimal totalPremium,
        String status,
        int insuredPeopleNumber,
        LocalDateTime applyDate,
        String termsUrl,
        String policyLink,
        Insurer insurer,
        Partner partner,
        Channel channel,
        InsurancePlan plan,
        InsurePeriod insurePeriod,
        AuthInfo auth,
        Contractor contractor,
        Payment payment,
        List<PersonSummary> people) {}
