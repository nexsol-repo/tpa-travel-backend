package com.nexsol.tpa.core.domain.city;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.domain.client.InsuranceReferenceClient;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 도시/국가 코드 검색 서비스.
 * MeritzReferenceClient로 API 호출 후 CityInfo로 변환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CitySearchService {

    private final InsuranceReferenceClient referenceClient;
    private final ObjectMapper objectMapper;

    public List<CityInfo> search(String keyword, String type) {
        BridgeApiResult res = referenceClient.getCityNationCodes(keyword, type);

        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.REFERENCE_API_FAILED,
                    "도시코드조회 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }

        return parseCityList(res.data());
    }

    private List<CityInfo> parseCityList(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            JsonNode node = objectMapper.readTree(json);
            JsonNode citiesNode = node.path("opapiTrvCityNatlInfCbcVo");

            if (citiesNode.isMissingNode() || !citiesNode.isArray()) {
                return List.of();
            }

            List<CityInfo> result = new ArrayList<>();
            for (JsonNode item : citiesNode) {
                result.add(
                        new CityInfo(
                                item.path("cityNatlCd").asText(null),
                                item.path("korNatlNm").asText(null),
                                item.path("engNatlNm").asText(null),
                                item.path("korCityNm").asText(null),
                                item.path("engCityNm").asText(null),
                                item.path("trvRskGrdeCd").asText(null),
                                null));
            }
            return result;
        } catch (Exception e) {
            log.error("[CITY_SEARCH] 응답 파싱 실패", e);
            throw new CoreException(CoreErrorType.REFERENCE_PARSE_FAILED, "도시코드 응답 파싱 실패");
        }
    }
}
