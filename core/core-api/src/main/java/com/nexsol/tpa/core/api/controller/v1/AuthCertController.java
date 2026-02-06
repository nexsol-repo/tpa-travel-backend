package com.nexsol.tpa.core.api.controller.v1;

import com.nexsol.tpa.core.api.dto.v1.AuthCertCompleteRequest;
import com.nexsol.tpa.core.api.dto.v1.AuthCertHistoryCompleteRequest;
import com.nexsol.tpa.core.api.dto.v1.AuthCertResultResponse;
import com.nexsol.tpa.core.api.service.v1.AuthCertService;
import com.nexsol.tpa.core.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthCertController {

    private final AuthCertService authCertService;

    @PostMapping("/cert/complete")
    public ApiResponse<AuthCertResultResponse> complete(@RequestBody AuthCertCompleteRequest req,
                                                        HttpServletRequest httpReq) {
        AuthCertResultResponse res = authCertService.complete(
                req,
                httpReq.getHeader("User-Agent"),
                extractClientIp(httpReq),
                httpReq.getHeader("Referer")
        );
        return ApiResponse.success(res);
    }

    @PostMapping("/cert/history/complete")
    public ApiResponse<AuthCertResultResponse> historyComplete(
            @RequestBody AuthCertHistoryCompleteRequest req,
            HttpServletRequest httpReq
    ) {
        AuthCertResultResponse res = authCertService.historyComplete(
                req,
                httpReq.getHeader("User-Agent"),
                extractClientIp(httpReq),
                httpReq.getHeader("Referer")
        );
        return ApiResponse.success(res);
    }


    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xrip = request.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();
        return request.getRemoteAddr();
    }
}
