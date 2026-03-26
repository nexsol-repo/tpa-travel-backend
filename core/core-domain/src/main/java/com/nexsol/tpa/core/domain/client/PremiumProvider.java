package com.nexsol.tpa.core.domain.client;

import com.nexsol.tpa.core.domain.premium.Premium;

/**
 * 보험료 산출 제공자 인터페이스.
 * 외부 API 호출 + 응답 파싱을 캡슐화하여 도메인 개념(Premium)을 반환한다.
 */
public interface PremiumProvider {

    Premium calculate(InsuranceQuoteClient.PremiumCommand command);
}
