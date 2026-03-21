package com.nexsol.tpa.client.portone;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "portone", url = "${portone.v1.base-url}")
public interface PortOneFeign {

    @PostMapping(value = "/users/getToken", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getToken(@RequestBody Map<String, String> body);

    @GetMapping("/certifications/{impUid}")
    String getCertification(
            @PathVariable("impUid") String impUid, @RequestHeader("Authorization") String token);
}
