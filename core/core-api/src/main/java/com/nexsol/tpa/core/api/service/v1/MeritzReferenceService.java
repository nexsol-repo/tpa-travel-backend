package com.nexsol.tpa.core.api.service.v1;

import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeRequest;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeResponse;
import com.nexsol.tpa.core.api.dto.CityResponseDto;
import com.nexsol.tpa.core.api.dto.MeritzCityItemDto;
import com.nexsol.tpa.core.api.dto.MeritzCityResponse;
import com.nexsol.tpa.core.api.meritz.config.CompaniesConfigsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeritzReferenceService {

    private static final String PLAN_INQ = "/b2b/v1/organ/meritz/planInq";
    private static final String CITY_NATION_INQ = "/b2b/v1/organ/meritz/citynatlcdInq";

    private final MeritzBridgeClient bridgeClient;
    private final CompaniesConfigsProperties companies;
    private final ObjectMapper objectMapper;

    public String getPlans(String stdDt) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve("TPA");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", "15540-19125");
        body.put("stdDt", stdDt);

        MeritzBridgeRequest req = new MeritzBridgeRequest(
                cfg.getCompanyCode(),
                PLAN_INQ,
                "POST",
                Map.of("Content-Type", "application/json; charset=UTF-8"),
                body
        );

        MeritzBridgeResponse res = bridgeClient.call(req);

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz planInq failed. meritzStatus=" + res.getStatus() + ", body=" + res.getBody());
        }
        return res.getBody();
    }

    public List<CityResponseDto> getCityNationCodes(String keyword, String type) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve("TPA");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("srchVal", keyword);
        body.put("srchCnd", type);
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());

        MeritzBridgeRequest req = new MeritzBridgeRequest(
                cfg.getCompanyCode(),
                CITY_NATION_INQ,
                "POST",
                Map.of("Content-Type", "application/json; charset=UTF-8"),
                body
        );

        MeritzBridgeResponse res = bridgeClient.call(req);

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz citynatlcdInq failed. meritzStatus=" + res.getStatus() + ", body=" + res.getBody());
        }

        try {
            MeritzCityResponse meritzResponse = objectMapper.readValue(res.getBody(), MeritzCityResponse.class);

            if (!"00001".equals(meritzResponse.getErrCd())) {
                throw new IllegalStateException("Meritz API error. errCd=" + meritzResponse.getErrCd());
            }

            List<MeritzCityItemDto> cities = Optional.ofNullable(meritzResponse.getCities()).orElse(List.of());

            return cities.stream()
                    .map(item -> new CityResponseDto(
                            item.getCityNatlCd(),
                            item.getKorNatlNm(),
                            item.getEngNatlNm(),
                            item.getKorCityNm(),
                            item.getEngCityNm(),
                            item.getTrvRskGrdeCd(),
                            null
                    ))
                    .toList();

        } catch (Exception e) {
            throw new IllegalStateException("Meritz citynatlcdInq parse failed. body=" + res.getBody(), e);
        }
    }

    private CompaniesConfigsProperties.CompanyConfig resolve(String companyCode) {
        if ("TPA".equalsIgnoreCase(companyCode)) return companies.getTpa();
        if ("INSBOON".equalsIgnoreCase(companyCode)) return companies.getInsboon();
        throw new IllegalArgumentException("Unknown companyCode: " + companyCode);
    }
}
