package com.nexsol.tpa.core.api.service.v1.impl;

import com.nexsol.tpa.core.api.config.AligoProperties;
import com.nexsol.tpa.core.api.config.AligoSmsClient;
import com.nexsol.tpa.core.api.dto.v1.TravelAlimtalkCompletedCommand;
import com.nexsol.tpa.core.api.service.v1.AlimtalkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimtalkServiceImpl implements AlimtalkService {

    private final AligoProperties props;

    private final AligoSmsClient client;

    @Override
    public void sendTravelContractCompleted(TravelAlimtalkCompletedCommand cmd) {

        // ✅ 스샷 문구 구조(문자용)
        String msg = """
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
                """.formatted(nvl(cmd.getReceiverName()), nvl(cmd.getProductName()), // 없으면
                                                                                     // ""
                                                                                     // 처리됨
                nvl(cmd.getPolicyNumber()), nvl(cmd.getCertificateUrl()), nvl(cmd.getTermsUrl()))
            .trim();

        MultiValueMap<String, String> form = AligoSmsClient.form();
        form.add("key", props.getApiKey());
        form.add("user_id", props.getUserId());
        form.add("sender", normalizeHp(props.getSender()));

        // 단일 수신자
        form.add("receiver", normalizeHp(cmd.getReceiverHp()));

        // 수신자 이름까지 넣고 싶으면 destination 사용(선택)
        // 예) receiver=010..., destination=010...|홍길동
        if (cmd.getReceiverHp() != null && cmd.getReceiverName() != null) {
            form.add("destination", normalizeHp(cmd.getReceiverHp()) + "|" + cmd.getReceiverName());
        }

        form.add("msg", msg);

        // title은 LMS에서만 의미 있지만 넣어두자
        form.add("title", "[가입 완료 안내]");

        // 테스트 모드 필요하면 주석 해제
        // form.add("testmode_yn", "Y");

        try {
            String res = client.send(form);
            log.info("[ALIGO-SMS] send ok. receiver={}, res={}", normalizeHp(cmd.getReceiverHp()), res);
        }
        catch (Exception e) {
            log.warn("[ALIGO-SMS] send fail. receiver={}, err={}", normalizeHp(cmd.getReceiverHp()), e.getMessage(), e);
        }
    }

    private String normalizeHp(String hp) {
        return hp == null ? "" : hp.replaceAll("[^0-9]", "");
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

}
