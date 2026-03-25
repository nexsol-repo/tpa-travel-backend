package com.nexsol.tpa.provider.meritz.premium;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceQuoteClient;
import com.nexsol.tpa.core.domain.client.InsuranceQuoteClient.PremiumCommand;
import com.nexsol.tpa.core.domain.client.PremiumProvider;
import com.nexsol.tpa.core.domain.premium.Premium;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzPremiumProvider implements PremiumProvider {

    private final InsuranceQuoteClient quoteClient;
    private final MeritzPremiumParser premiumParser;
    private final ObjectMapper objectMapper;

    @Override
    public Premium calculate(PremiumCommand command) {
        String rawJson = quoteClient.calculatePremium(command);
        if (rawJson == null) {
            return null;
        }

        try {
            JsonNode data = objectMapper.readTree(rawJson);
            return premiumParser.parse(data, command);
        } catch (Exception e) {
            log.error("[MERITZ_PROVIDER] 응답 파싱 실패", e);
            return null;
        }
    }
}
