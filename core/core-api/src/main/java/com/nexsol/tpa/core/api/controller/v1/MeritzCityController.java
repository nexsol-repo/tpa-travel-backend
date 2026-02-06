package com.nexsol.tpa.core.api.controller.v1;

import com.nexsol.tpa.core.api.dto.v1.CityResponseDto;
import com.nexsol.tpa.core.api.service.v1.FavoriteCityService;
import com.nexsol.tpa.core.api.service.v1.MeritzReferenceService;
import com.nexsol.tpa.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/meritz")
@RequiredArgsConstructor
public class MeritzCityController {

    private final FavoriteCityService favoriteCityService;

    private final MeritzReferenceService referenceService;

    @GetMapping("/favorite-cities")
    public ApiResponse<List<CityResponseDto>> getFavoriteCities() {
        return ApiResponse.success(favoriteCityService.getFavoriteCities());
    }

    @GetMapping("/meritz-cities")
    public ApiResponse<List<CityResponseDto>> getMeritzCities(@RequestParam String keyword,
            @RequestParam(defaultValue = "2") String type) {
        return ApiResponse.success(referenceService.getCityNationCodes(keyword, type));
    }

}
