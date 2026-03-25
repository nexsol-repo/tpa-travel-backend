package com.nexsol.tpa.core.domain.city;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.CityProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitySearchService {

    private final CityProvider cityProvider;

    public List<CityInfo> search(String keyword, String type) {
        return cityProvider.searchCities(keyword, type);
    }
}
