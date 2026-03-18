package com.nexsol.tpa.core.domain.city;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.reference.MeritzReferenceClient;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;

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

    private final MeritzReferenceClient referenceClient;
    private final ObjectMapper objectMapper;

    public List<CityInfo> search(String keyword, String type) {
        MeritzBridgeApiResponse res = referenceClient.getCityNationCodes(keyword, type);

        if (!res.isSuccess()) {
            throw new CoreApiException(
                    CoreApiErrorType.REFERENCE_API_FAILED,
                    "도시코드조회 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }

        return parseCityList(res.getData());
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
            throw new CoreApiException(CoreApiErrorType.REFERENCE_PARSE_FAILED, "도시코드 응답 파싱 실패");
        }
    }
}
