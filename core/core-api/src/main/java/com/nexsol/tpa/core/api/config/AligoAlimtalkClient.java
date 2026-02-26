package com.nexsol.tpa.core.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AligoAlimtalkClient {

    private static final String SEND_URL = "https://kakaoapi.aligo.in/akv10/alimtalk/send/";

    private final @org.springframework.beans.factory.annotation.Qualifier("aligoWebClient") WebClient webClient;

    public String send(MultiValueMap<String, String> form) {
        return webClient.post()
            .uri(SEND_URL)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(form)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    public static MultiValueMap<String, String> form() {
        return new LinkedMultiValueMap<>();
    }

}
