package com.nexsol.tpa.core.api.controller.v1;

import jakarta.validation.Valid;
import com.nexsol.tpa.core.api.dto.QuoteRequest;
import com.nexsol.tpa.core.api.dto.QuoteResponse;
import com.nexsol.tpa.core.api.service.v1.MeritzQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/meritz/travel")
@RequiredArgsConstructor
public class MeritzQuotationController {

    private final MeritzQuotationService quoteService;

    @PostMapping(value = "/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return quoteService.quote(request);
    }

}
