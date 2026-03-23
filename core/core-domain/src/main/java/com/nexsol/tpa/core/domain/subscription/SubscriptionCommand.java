package com.nexsol.tpa.core.domain.subscription;

public record SubscriptionCommand(
        Long contractId, String cardNo, String efctPrd, String dporNm, String dporCd) {}
