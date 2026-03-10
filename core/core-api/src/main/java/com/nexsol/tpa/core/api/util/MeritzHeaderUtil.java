package com.nexsol.tpa.core.api.meritz.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class MeritzHeaderUtil {

    private static final DateTimeFormatter TRAN_ID_DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final DateTimeFormatter TS_DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    // 프로세스 내에서만 증가. (멀티 인스턴스면 Redis/DB 시퀀스 고려)
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private MeritzHeaderUtil() {
    }

    /** 예: TPA2026011401210001 */
    public static String generateTranId(String aflcoDivCd) {
        LocalDateTime now = LocalDateTime.now();
        int seq = nextSeq0001();
        return aflcoDivCd + now.format(TRAN_ID_DT) + String.format("%04d", seq);
    }

    /** 예: 20260114012115000 */
    public static String generateTimestamp() {
        return LocalDateTime.now().format(TS_DT);
    }

    private static int nextSeq0001() {
        int v = SEQ.getAndIncrement();
        if (v > 9999) {
            SEQ.set(1);
            v = SEQ.getAndIncrement();
        }
        return v;
    }

}
