package com.nexsol.tpa.client.aligo;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.nexsol.tpa.core.domain.client.NotificationClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AligoNotificationClient implements NotificationClient {

    private final AligoAlimtalkFeign alimtalkFeign;
    private final AligoSmsFeign smsFeign;
    private final AligoProperties properties;

    @Override
    public String sendAlimtalk(Map<String, String> params) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        params.forEach(form::add);
        return alimtalkFeign.send(form);
    }

    @Override
    public String sendSms(Map<String, String> params) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        params.forEach(form::add);
        return smsFeign.send(form);
    }

    @Override
    public String getApiKey() {
        return properties.getApiKey();
    }

    @Override
    public String getUserId() {
        return properties.getUserId();
    }

    @Override
    public String getSender() {
        return properties.getSender();
    }
}
