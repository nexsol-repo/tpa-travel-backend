package com.nexsol.tpa.client.aligo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aligo-alimtalk", url = "https://kakaoapi.aligo.in")
public interface AligoAlimtalkFeign {

    @PostMapping(
            value = "/akv10/alimtalk/send/",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String send(@RequestBody MultiValueMap<String, String> form);
}
