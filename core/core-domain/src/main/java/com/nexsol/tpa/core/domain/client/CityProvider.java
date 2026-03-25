package com.nexsol.tpa.core.domain.client;

import java.util.List;

import com.nexsol.tpa.core.domain.city.CityInfo;

public interface CityProvider {
    List<CityInfo> searchCities(String keyword, String type);
}
