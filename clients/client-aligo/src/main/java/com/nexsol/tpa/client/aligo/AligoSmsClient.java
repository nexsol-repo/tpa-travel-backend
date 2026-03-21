package com.nexsol.tpa.client.aligo;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AligoSmsClient {

    private final AligoSmsFeign aligoSmsFeign;

    public String send(MultiValueMap<String, String> form) {
        return aligoSmsFeign.send(form);
    }

    public static MultiValueMap<String, String> form() {
        return new LinkedMultiValueMap<>();
    }
}
