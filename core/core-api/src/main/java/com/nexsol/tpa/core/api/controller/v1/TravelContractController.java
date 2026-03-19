package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.response.ContractQueryResponse;
import com.nexsol.tpa.core.domain.contract.TravelContractQueryService;
import com.nexsol.tpa.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/meritz/travel")
@RequiredArgsConstructor
public class TravelContractController {

    private final TravelContractQueryService queryService;

    /** 계약 목록 (auth_unique_key 최신순 기본) */
    @GetMapping("/contracts")
    public ApiResponse<Page<ContractQueryResponse.ContractListItem>> list(
            @RequestParam(required = false) String authUniqueKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(queryService.list(authUniqueKey, page, size));
    }

    /** 계약 단건 조회 (id) */
    @GetMapping("/contracts/{id}")
    public ApiResponse<ContractQueryResponse.ContractDetail> get(@PathVariable Long id) {
        return ApiResponse.success(queryService.get(id));
    }
}