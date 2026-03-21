package com.nexsol.tpa.core.domain.alimtalk;

public record AlimtalkCompletedCommand(
        String receiverHp,
        String receiverName,
        String productName,
        String policyNumber,
        String certificateUrl,
        String termsUrl) {}
