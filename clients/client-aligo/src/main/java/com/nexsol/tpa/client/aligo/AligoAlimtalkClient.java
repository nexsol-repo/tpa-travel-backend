package com.nexsol.tpa.client.aligo;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AligoAlimtalkClient {

    private final AligoAlimtalkFeign aligoAlimtalkFeign;

    public String send(MultiValueMap<String, String> form) {
        return aligoAlimtalkFeign.send(form);
    }

    public static MultiValueMap<String, String> form() {
        return new LinkedMultiValueMap<>();
    }
}
