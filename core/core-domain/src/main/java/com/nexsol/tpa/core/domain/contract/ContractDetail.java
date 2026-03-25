package com.nexsol.tpa.core.domain.contract;

import java.util.List;

import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;

import lombok.Builder;

@Builder
public record ContractDetail(
        ContractInfo contract, Payment payment, List<InsuredPerson> people, InsurancePlan plan) {}
