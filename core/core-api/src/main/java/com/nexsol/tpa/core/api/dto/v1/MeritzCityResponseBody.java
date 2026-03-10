package com.nexsol.tpa.core.api.dto.v1;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MeritzCityResponseBody {

    private List<MeritzCityItemDto> list;

}
