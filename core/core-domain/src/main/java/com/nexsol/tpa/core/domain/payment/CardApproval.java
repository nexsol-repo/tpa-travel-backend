package com.nexsol.tpa.core.domain.payment;

import lombok.Builder;

@Builder
public record CardApproval(String approvalNumber, String approvalDate) {}
