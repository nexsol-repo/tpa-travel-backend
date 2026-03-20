package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.AuthCertCompleteRequest;
import com.nexsol.tpa.core.api.controller.v1.request.AuthCertHistoryCompleteRequest;
import com.nexsol.tpa.core.api.controller.v1.response.AuthCertResultResponse;
import com.nexsol.tpa.core.domain.auth.AuthCertHistory;
import com.nexsol.tpa.core.domain.auth.AuthCertResult;
import com.nexsol.tpa.core.domain.auth.AuthCertService;
import com.nexsol.tpa.core.domain.auth.AuthCertification;
import com.nexsol.tpa.core.support.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthCertController {

    private final AuthCertService authCertService;

    @PostMapping("/cert/complete")
    public ApiResponse<AuthCertResultResponse> complete(
            @RequestBody AuthCertCompleteRequest req, HttpServletRequest httpReq) {
        var cmd =
                new AuthCertification(
                        req.getContractId(),
                        req.getImpUid(),
                        req.getRequestId(),
                        req.getMoid(),
                        req.getBizNum(),
                        req.getPathRoot(),
                        req.getPg(),
                        req.getProvider());
        AuthCertResult result =
                authCertService.complete(
                        cmd,
                        httpReq.getHeader("User-Agent"),
                        extractClientIp(httpReq),
                        httpReq.getHeader("Referer"));
        return ApiResponse.success(AuthCertResultResponse.of(result));
    }

    @PostMapping("/cert/history/complete")
    public ApiResponse<AuthCertResultResponse> historyComplete(
            @RequestBody AuthCertHistoryCompleteRequest req, HttpServletRequest httpReq) {
        var cmd =
                new AuthCertHistory(
                        req.getImpUid(),
                        req.getRequestId(),
                        req.getMoid(),
                        req.getPg(),
                        req.getProvider(),
                        req.getPathRoot(),
                        req.getBizNum());
        AuthCertResult result =
                authCertService.historyComplete(
                        cmd,
                        httpReq.getHeader("User-Agent"),
                        extractClientIp(httpReq),
                        httpReq.getHeader("Referer"));
        return ApiResponse.success(AuthCertResultResponse.of(result));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xrip = request.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();
        return request.getRemoteAddr();
    }
}
