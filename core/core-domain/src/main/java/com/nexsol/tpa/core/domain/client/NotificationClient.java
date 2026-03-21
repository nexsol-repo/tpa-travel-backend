package com.nexsol.tpa.core.domain.client;

import java.util.Map;

public interface NotificationClient {

    String sendAlimtalk(Map<String, String> params);

    String sendSms(Map<String, String> params);

    String getApiKey();

    String getUserId();

    String getSender();
}
