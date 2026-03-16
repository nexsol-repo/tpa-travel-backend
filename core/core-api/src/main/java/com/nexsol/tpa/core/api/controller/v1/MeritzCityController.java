package com.nexsol.tpa.core.api.controller.v1;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexsol.tpa.core.api.controller.v1.response.CityResponse;
import com.nexsol.tpa.core.domain.city.CitySearchService;
import com.nexsol.tpa.core.domain.city.FavoriteCityService;
import com.nexsol.tpa.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/meritz")
@RequiredArgsConstructor
public class MeritzCityController {

    private final FavoriteCityService favoriteCityService;

    private final CitySearchService citySearchService;

    @GetMapping("/favorite-cities")
    public ApiResponse<List<CityResponse>> getFavoriteCities() {
        var cities =
                favoriteCityService.getFavoriteCities().stream().map(CityResponse::of).toList();
        return ApiResponse.success(cities);
    }

    @GetMapping("/meritz-cities")
    public ApiResponse<List<CityResponse>> getMeritzCities(
            @RequestParam String keyword, @RequestParam(defaultValue = "2") String type) {
        var cities =
                citySearchService.search(keyword, type).stream().map(CityResponse::of).toList();
        return ApiResponse.success(cities);
    }
}
