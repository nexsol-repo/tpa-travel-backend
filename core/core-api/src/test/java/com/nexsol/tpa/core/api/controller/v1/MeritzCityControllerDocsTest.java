package com.nexsol.tpa.core.api.controller.v1;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.domain.city.CityInfo;
import com.nexsol.tpa.core.domain.city.CitySearchService;
import com.nexsol.tpa.core.domain.city.FavoriteCityService;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class MeritzCityControllerDocsTest extends RestDocsTest {

    private final FavoriteCityService favoriteCityService = mock(FavoriteCityService.class);
    private final CitySearchService citySearchService = mock(CitySearchService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc =
                mockController(new MeritzCityController(favoriteCityService, citySearchService));
    }

    @Test
    void getFavoriteCities() throws Exception {
        when(favoriteCityService.getFavoriteCities()).thenReturn(sampleCities());

        mockMvc.perform(get("/v1/meritz/favorite-cities"))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-favorite-cities",
                                responseFields(
                                        fieldWithPath("result").type(STRING).description("결과"),
                                        fieldWithPath("data").type(ARRAY).description("도시 목록"),
                                        fieldWithPath("error")
                                                .type(OBJECT)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("data[].cityNatlCd")
                                                .type(STRING)
                                                .description("도시/국가 코드"),
                                        fieldWithPath("data[].korNatlNm")
                                                .type(STRING)
                                                .description("국가명(한글)"),
                                        fieldWithPath("data[].engNatlNm")
                                                .type(STRING)
                                                .description("국가명(영문)"),
                                        fieldWithPath("data[].korCityNm")
                                                .type(STRING)
                                                .description("도시명(한글)"),
                                        fieldWithPath("data[].engCityNm")
                                                .type(STRING)
                                                .description("도시명(영문)"),
                                        fieldWithPath("data[].trvRskGrdeCd")
                                                .type(STRING)
                                                .description("여행위험등급코드"),
                                        fieldWithPath("data[].sortOrder")
                                                .type(NUMBER)
                                                .description("정렬순서"))));
    }

    @Test
    void getMeritzCities() throws Exception {
        when(citySearchService.search(eq("일본"), eq("2"))).thenReturn(sampleCities());

        mockMvc.perform(get("/v1/meritz/meritz-cities").param("keyword", "일본").param("type", "2"))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-cities",
                                queryParameters(
                                        parameterWithName("keyword").description("검색 키워드"),
                                        parameterWithName("type")
                                                .description("검색 타입 (기본값: 2)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("result").type(STRING).description("결과"),
                                        fieldWithPath("data").type(ARRAY).description("도시 목록"),
                                        fieldWithPath("error")
                                                .type(OBJECT)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("data[].cityNatlCd")
                                                .type(STRING)
                                                .description("도시/국가 코드"),
                                        fieldWithPath("data[].korNatlNm")
                                                .type(STRING)
                                                .description("국가명(한글)"),
                                        fieldWithPath("data[].engNatlNm")
                                                .type(STRING)
                                                .description("국가명(영문)"),
                                        fieldWithPath("data[].korCityNm")
                                                .type(STRING)
                                                .description("도시명(한글)"),
                                        fieldWithPath("data[].engCityNm")
                                                .type(STRING)
                                                .description("도시명(영문)"),
                                        fieldWithPath("data[].trvRskGrdeCd")
                                                .type(STRING)
                                                .description("여행위험등급코드"),
                                        fieldWithPath("data[].sortOrder")
                                                .type(NUMBER)
                                                .description("정렬순서"))));
    }

    private List<CityInfo> sampleCities() {
        return List.of(
                new CityInfo("JP", "일본", "JAPAN", "도쿄", "TOKYO", "1", 1),
                new CityInfo("JP", "일본", "JAPAN", "오사카", "OSAKA", "1", 2));
    }
}
