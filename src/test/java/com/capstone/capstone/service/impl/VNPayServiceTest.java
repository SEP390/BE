package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {
    private static final String MOCK_HASH_SECRET = "MOCK_SECRET_KEY_123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    private static final String MOCK_TMNCODE = "TESTTMN";
    private static final String EXPECTED_RETURN_URL = "http://localhost:5173/vnpay";
    @InjectMocks
    private VNPayService vnPayService;
    @Mock
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vnPayService, "HASH_SECRET", MOCK_HASH_SECRET);
        ReflectionTestUtils.setField(vnPayService, "TMNCODE", MOCK_TMNCODE);
    }

    @Test
    @DisplayName("createPaymentUrl: Should generate the correct payment URL with required parameters and hash")
    void createPaymentUrl_Success() {
        UUID id = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
        LocalDateTime createDate = LocalDateTime.of(2025, 11, 26, 10, 30, 0);
        long amount = 100000;
        String resultUrl = vnPayService.createPaymentUrl(id, createDate, amount);
        assertThat(resultUrl).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        assertThat(resultUrl).contains("vnp_TmnCode=" + MOCK_TMNCODE);
        assertThat(resultUrl).contains("vnp_Amount=10000000");
        assertThat(resultUrl).contains("vnp_TxnRef=" + id.toString());
        assertThat(resultUrl).contains("vnp_CreateDate=20251126103000");
        assertThat(resultUrl).contains("vnp_ExpireDate=20251126104000");
        assertThat(resultUrl).contains("vnp_ReturnUrl=" + EXPECTED_RETURN_URL);
        assertThat(resultUrl).contains("&vnp_SecureHash=");
        String secureHash = resultUrl.substring(resultUrl.lastIndexOf("=") + 1);
        assertThat(secureHash).hasSize(128);
    }

    @Test
    @DisplayName("encodeParams: Should sort parameters and encode values correctly")
    void encodeParams_ShouldSortAndEncode() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("zKey", "Z Value");
        params.put("aKey", "A Value with spaces");
        params.put("bKey", "BValue");
        String expected = "aKey=A%20Value%20with%20spaces&bKey=BValue&zKey=Z%20Value";
        String encoded = vnPayService.encodeParams(params);
        assertThat(encoded).isEqualTo(expected);
    }

    @Test
    @DisplayName("getAllParams: Should extract all parameters from HttpServletRequest")
    void getAllParams_Success() {
        when(mockRequest.getParameterNames()).thenReturn(Collections.enumeration(List.of("p1", "p2", "vnp_SecureHash")));
        when(mockRequest.getParameter("p1")).thenReturn("value1");
        when(mockRequest.getParameter("p2")).thenReturn("value2");
        when(mockRequest.getParameter("vnp_SecureHash")).thenReturn("hashvalue");
        java.util.Map<String, String> params = vnPayService.getAllParams(mockRequest);
        assertThat(params)
                .hasSize(3)
                .containsEntry("p1", "value1")
                .containsEntry("p2", "value2")
                .containsEntry("vnp_SecureHash", "hashvalue");
    }

    @Test
    @DisplayName("verify: Should return SUCCESS status when signature is valid and transaction status is '00'")
    void verify_Success() {
        UUID txnRef = UUID.randomUUID();
        java.util.Map<String, String> validParams = new java.util.LinkedHashMap<>();
        validParams.put("vnp_Amount", "1000000");
        validParams.put("vnp_BankCode", "NCB");
        validParams.put("vnp_TxnRef", txnRef.toString());
        validParams.put("vnp_TransactionStatus", "00");
        validParams.put("vnp_SecureHash", "8828b80b064c3997c4132479f6484964177d9c66e2c48261899147e4088a531e21b777a988d5786134b285b03517c5b6ff4f22c151475c40480398ddc5496464");
        when(mockRequest.getParameterNames()).thenReturn(Collections.enumeration(validParams.keySet()));
        validParams.forEach((key, value) -> when(mockRequest.getParameter(key)).thenReturn(value));
        when(mockRequest.getParameter("vnp_TransactionStatus")).thenReturn("00");
        VNPayResult result = vnPayService.verify(mockRequest);
        assertThat(result.getStatus()).isEqualTo(VNPayStatus.SUCCESS);
        assertThat(result.getId()).isEqualTo(txnRef);
    }

    @Test
    @DisplayName("verify: Should return INVALID_SIGNATURE status when hash is invalid")
    void verify_InvalidSignature() {
        UUID txnRef = UUID.randomUUID();
        java.util.Map<String, String> invalidParams = new java.util.LinkedHashMap<>();
        invalidParams.put("vnp_Amount", "1000000");
        invalidParams.put("vnp_BankCode", "NCB");
        invalidParams.put("vnp_TxnRef", txnRef.toString());
        invalidParams.put("vnp_SecureHash", "INVALID_HASH");
        when(mockRequest.getParameterNames()).thenReturn(Collections.enumeration(invalidParams.keySet()));
        invalidParams.forEach((key, value) -> when(mockRequest.getParameter(key)).thenReturn(value));
        VNPayResult result = vnPayService.verify(mockRequest);
        assertThat(result.getStatus()).isEqualTo(VNPayStatus.INVALID_SIGNATURE);
        assertThat(result.getId()).isEqualTo(txnRef);
    }

    @Test
    @DisplayName("verify: Should return CANCEL status when transaction status is not '00'")
    void verify_CancelStatus() {
        UUID txnRef = UUID.randomUUID();
        String validHash = "8828b80b064c3997c4132479f6484964177d9c66e2c48261899147e4088a531e21b777a988d5786134b285b03517c5b6ff4f22c151475c40480398ddc5496464";
        java.util.Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("vnp_Amount", "1000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_TxnRef", txnRef.toString());
        params.put("vnp_TransactionStatus", "02");
        params.put("vnp_SecureHash", validHash);
        when(mockRequest.getParameterNames()).thenReturn(Collections.enumeration(params.keySet()));
        params.forEach((key, value) -> when(mockRequest.getParameter(key)).thenReturn(value));
        when(mockRequest.getParameter("vnp_TransactionStatus")).thenReturn("02");
        VNPayResult result = vnPayService.verify(mockRequest);
        assertThat(result.getStatus()).isEqualTo(VNPayStatus.CANCEL);
    }

    @Test
    @DisplayName("hmacSHA512: Should return correct hash for a given key and data")
    void hmacSHA512_Valid() {
        String key = "test_key";
        String data = "test_data";
        String expectedHash = "7f502d33457a4687d0e408544d9f67645f8f8b725c777d4c0c1b4b1a41578a1f6a1e360980c574044a0e9102c40c8f94d9b4b92b677a284c8a514d7971b48b11";
        String actualHash = VNPayService.hmacSHA512(key, data);
        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test
    @DisplayName("hmacSHA512: Should handle null key and data gracefully by returning empty string")
    void hmacSHA512_NullInput() {
        assertThat(VNPayService.hmacSHA512(null, "data")).isEmpty();
        assertThat(VNPayService.hmacSHA512("key", null)).isEmpty();
        assertThat(VNPayService.hmacSHA512(null, null)).isEmpty();
    }
}