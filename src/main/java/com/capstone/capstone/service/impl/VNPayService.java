package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonWriter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VNPayService {
    @Value("${vnpay.secret}")
    private String HASH_SECRET;
    @Value("${vnpay.tmncode}")
    private String TMNCODE;

    private static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String QUERY_URL = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    private static final String RETURN_URL = "http://localhost:5173/vnpay";

    public String createPaymentUrl(UUID id, LocalDateTime createDate, long amount) {
        amount = amount * 100;
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TxnRef", id.toString());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_OrderType", "other");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_OrderInfo", "dorm");
        params.put("vnp_Locale", "vn");
        params.put("vnp_TmnCode", TMNCODE);
        params.put("vnp_ReturnUrl", RETURN_URL);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(createDate);
        params.put("vnp_CreateDate", vnp_CreateDate);
        LocalDateTime expire = createDate.plusMinutes(10);
        String vnp_ExpireDate = formatter.format(expire);
        params.put("vnp_ExpireDate", vnp_ExpireDate);
        String encodedParams = encodeParams(params);
        String vnp_SecureHash = hashParams(encodedParams);
        String paymentUrl = PAY_URL + "?" + encodedParams + "&vnp_SecureHash=" + vnp_SecureHash;
        return paymentUrl;
    }

    public String encodeParams(Map<String, String> params) {
        return params.keySet().stream()
                .sorted(String::compareTo)
                .map(p -> p + "=" + URLEncoder.encode(params.get(p), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public Map<String, String> getAllParams(HttpServletRequest req) {
        final Map<String, String> fields = new HashMap<>();
        req.getParameterNames().asIterator().forEachRemaining(param -> fields.put(param, req.getParameter(param)));
        return fields;
    }

    public VNPayResult verify(HttpServletRequest req) {
        final Map<String, String> params = getAllParams(req);
        var result = new VNPayResult();
        result.setId(UUID.fromString(params.get("vnp_TxnRef")));
        String vnp_SecureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        String encodedParams = encodeParams(params);
        String hashed = hashParams(encodedParams);
        if (!hashed.equals(vnp_SecureHash)) {
            result.setStatus(VNPayStatus.INVALID_SIGNATURE);
            return result;
        }
        if("00".equals(req.getParameter("vnp_TransactionStatus"))) {
            result.setStatus(VNPayStatus.SUCCESS);
            return result;
        }
        result.setStatus(VNPayStatus.CANCEL);
        return result;
    }
    
    public String queryPaymentResult(UUID id, String vnp_TransDate) {
        String vnp_RequestId = UUID.randomUUID().toString().replace("-", "");
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = TMNCODE;
        String vnp_TxnRef = id.toString();
        String vnp_OrderInfo = UUID.randomUUID().toString().replace("-", "");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        String vnp_IpAddr = "127.0.0.1";
        Map<String, String>  params = new HashMap<>();
        params.put("vnp_RequestId", vnp_RequestId);
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo", vnp_OrderInfo);
        params.put("vnp_TransactionDate", vnp_TransDate);
        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_IpAddr", vnp_IpAddr);
        String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
        String vnp_SecureHash = hmacSHA512(HASH_SECRET, hash_Data);
        params.put("vnp_SecureHash", vnp_SecureHash);
        return RestClient.create(QUERY_URL).post().body(JsonWriter.standard().writeToString(params)).contentType(MediaType.APPLICATION_JSON).retrieve().body(String.class);
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
