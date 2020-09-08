package com.touchbiz.gateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

@Slf4j
public class SignatureUtils {

    private SignatureUtils() {
    }

    public static String sign(String url, String key, String secret, String timestamp, String body) {
        try {
            String text = String.format("%s%s%s%s", url, key, timestamp, body);
            String sign = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret).hmacHex(text);
            log.info("原始数据:{}\n签名:{}", text, sign);
            return sign;
        } catch (Exception e) {
            log.error("签名失败", e);
            return "";
        }
    }
}
