package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.List;

import com.nexsol.tpa.core.domain.contract.Channel;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.Partner;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;

import lombok.Builder;

@Builder
public record ContractDetail(
        ContractInfo contract,
        Insurer insurer,
        Partner partner,
        Channel channel,
        InsurancePlan plan,
        Payment payment,
        String termsUrl,
        String policyLink,
        List<InsuredPersonDetail> people) {}
