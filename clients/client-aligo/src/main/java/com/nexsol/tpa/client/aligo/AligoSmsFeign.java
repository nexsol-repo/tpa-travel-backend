package com.nexsol.tpa.client.aligo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aligo-sms", url = "https://apis.aligo.in")
public interface AligoSmsFeign {

    @PostMapping(value = "/send/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String send(@RequestBody MultiValueMap<String, String> form);
}
