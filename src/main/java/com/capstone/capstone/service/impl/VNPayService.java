package com.capstone.capstone.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VNPayService {
    @Value("${vnpay.secret}")
    private String HASH_SECRET;
    @Value("${vnpay.tmncode}")
    private String TMNCODE;

    private static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    public String createPaymentUrl(UUID id, long amount) {
        String returnUrl = "http://localhost:5173/vnpay";
        amount = amount * 100;
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TxnRef", id.toString());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_OrderType", "other");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_OrderInfo", "wallet");
        params.put("vnp_Locale", "vn");
        params.put("vnp_TmnCode", TMNCODE);
        params.put("vnp_ReturnUrl", returnUrl);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC+7"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(now);
        params.put("vnp_CreateDate", vnp_CreateDate);
        ZonedDateTime expire = now.plusMinutes(10);
        String vnp_ExpireDate = formatter.format(expire);
        params.put("vnp_ExpireDate", vnp_ExpireDate);
        String encodedParams = encodeParams(params);
        String vnp_SecureHash = hashParams(encodedParams);
        return PAY_URL + "?" + encodedParams + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    public String encodeParams(Map<String, String> params) {
        return params.keySet().stream()
                .sorted(String::compareTo)
                .map(p -> p + "=" + URLEncoder.encode(params.get(p), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public String hashParams(String encodedParams) {
        return hmacSHA512(HASH_SECRET, encodedParams);
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (IllegalStateException | NullPointerException | InvalidKeyException | NoSuchAlgorithmException ex) {
            return "";
        }
    }
}
