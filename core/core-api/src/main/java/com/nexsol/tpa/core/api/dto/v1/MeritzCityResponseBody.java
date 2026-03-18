package com.nexsol.tpa.core.api.dto.v1;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeritzCityResponseBody {

    private List<MeritzCityItemDto> list;
}
