package com.nexsol.tpa.core.domain.alimtalk;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.NotificationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimtalkSenderImpl implements AlimtalkSender {

    private final NotificationClient notificationClient;

    @Override
    public void sendTravelContractCompleted(AlimtalkCompletedCommand cmd) {

        String msg =
                """
                안녕하세요. %s 님
                (주)티피에이코리아입니다.
                %s 정상 가입되었습니다.
                [증권번호 %s]

                아래 URL을 통해 증권/가입확인서를 확인해 주시기 바랍니다.
                %s

                약관보기
                %s

                기타 궁금하신 사항은 아래 문의처로 연락 바랍니다.
                ※문의처
                (주)티피에이코리아 대표번호  02-3472-7925
                (평일 9시~18시/점심시간 12시~13시, 공휴일 제외)
                """
                        .formatted(
                                nvl(cmd.receiverName()),
                                nvl(cmd.productName()),
                                nvl(cmd.policyNumber()),
                                nvl(cmd.certificateUrl()),
                                nvl(cmd.termsUrl()))
                        .trim();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("key", notificationClient.getApiKey());
        params.put("user_id", notificationClient.getUserId());
        params.put("sender", normalizeHp(notificationClient.getSender()));

        params.put("receiver", normalizeHp(cmd.receiverHp()));

        if (cmd.receiverHp() != null && cmd.receiverName() != null) {
            params.put("destination", normalizeHp(cmd.receiverHp()) + "|" + cmd.receiverName());
        }

        params.put("msg", msg);
        params.put("title", "[가입 완료 안내]");

        try {
            String res = notificationClient.sendSms(params);
            log.info(
                    "[ALIGO-SMS] send ok. receiver={}, res={}", normalizeHp(cmd.receiverHp()), res);
        } catch (Exception e) {
            log.warn(
                    "[ALIGO-SMS] send fail. receiver={}, err={}",
                    normalizeHp(cmd.receiverHp()),
                    e.getMessage(),
                    e);
        }
    }

    private String normalizeHp(String hp) {
        return hp == null ? "" : hp.replaceAll("[^0-9]", "");
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
