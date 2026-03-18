package com.nexsol.tpa.client.meritz.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import com.nexsol.tpa.client.meritz.config.MeritzTpaProperties;

public final class MeritzHeaderFactory {

    private static final DateTimeFormatter TRAN_ID_DT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final DateTimeFormatter TS_DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private MeritzHeaderFactory() {}

    public static String xApiTranId(MeritzTpaProperties props) {
        int seq = nextSeq0001();
        return props.getAflcoDivCd()
                + LocalDateTime.now().format(TRAN_ID_DT)
                + String.format("%04d", seq);
    }

    public static String timestamp() {
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
