package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.PaymentResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private VNPayService vnPayService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private InvoiceChangeService invoiceChangeService;

    @InjectMocks
    private PaymentService paymentService;

    private Payment mockPayment;
    private Invoice mockInvoice;
    private VNPayResult mockVNPayResult;
    private PaymentResponse mockPaymentResponse;

    @BeforeEach
    void setUp() {
        mockInvoice = new Invoice();
        mockInvoice.setId(UUID.randomUUID());
        mockInvoice.setStatus(PaymentStatus.PENDING);
        mockInvoice.setPrice(1000000L);
        mockInvoice.setExpireTime(LocalDateTime.now().plusMinutes(10));

        mockPayment = new Payment();
        mockPayment.setId(UUID.randomUUID());
        mockPayment.setInvoice(mockInvoice);
        mockPayment.setStatus(PaymentStatus.PENDING);
        mockPayment.setPrice(1000000L);
        mockPayment.setCreateTime(LocalDateTime.now());

        mockVNPayResult = new VNPayResult();
        mockVNPayResult.setId(mockPayment.getId());
        mockVNPayResult.setStatus(VNPayStatus.SUCCESS);

        mockPaymentResponse = new PaymentResponse();
        mockPaymentResponse.setStatus(PaymentStatus.SUCCESS);
    }

    // ========== handle(VNPayResult) Tests ==========

    @Test
    void handle_VNPayResult_Success_UpdatesToSuccess() {
        // Arrange
        when(paymentRepository.findById(mockPayment.getId())).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(modelMapper.map(any(Payment.class), eq(PaymentResponse.class))).thenReturn(mockPaymentResponse);
        when(invoiceChangeService.update(any(Invoice.class), eq(PaymentStatus.SUCCESS))).thenReturn(mockInvoice);

        // Act
        PaymentResponse result = paymentService.handle(mockVNPayResult);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus());
        verify(invoiceChangeService).update(mockInvoice, PaymentStatus.SUCCESS);
    }

    @Test
    void handle_VNPayResult_Failed_UpdatesToCancel() {
        // Arrange
        mockVNPayResult.setStatus(VNPayStatus.CANCEL);
        when(paymentRepository.findById(mockPayment.getId())).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(modelMapper.map(any(Payment.class), eq(PaymentResponse.class))).thenReturn(mockPaymentResponse);
        when(invoiceChangeService.update(any(Invoice.class), eq(PaymentStatus.CANCEL))).thenReturn(mockInvoice);

        // Act
        PaymentResponse result = paymentService.handle(mockVNPayResult);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.CANCEL, paymentCaptor.getValue().getStatus());
        verify(invoiceChangeService).update(mockInvoice, PaymentStatus.CANCEL);
    }

    @Test
    void handle_VNPayResult_AlreadyProcessed_DoesNotUpdate() {
        // Arrange
        mockInvoice.setStatus(PaymentStatus.SUCCESS);
        mockPayment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(mockPayment.getId())).thenReturn(Optional.of(mockPayment));
        when(modelMapper.map(any(Payment.class), eq(PaymentResponse.class))).thenReturn(mockPaymentResponse);

        // Act
        PaymentResponse result = paymentService.handle(mockVNPayResult);

        // Assert
        assertNotNull(result);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(invoiceChangeService, never()).update(any(Invoice.class), any(PaymentStatus.class));
    }

    @Test
    void handle_VNPayResult_PaymentNotFound_ThrowsException() {
        // Arrange
        when(paymentRepository.findById(mockPayment.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> paymentService.handle(mockVNPayResult));
    }

    // ========== handle(HttpServletRequest) Tests ==========

    @Test
    void handle_HttpServletRequest_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(vnPayService.verify(request)).thenReturn(mockVNPayResult);
        when(paymentRepository.findById(mockPayment.getId())).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(modelMapper.map(any(Payment.class), eq(PaymentResponse.class))).thenReturn(mockPaymentResponse);
        when(invoiceChangeService.update(any(Invoice.class), any(PaymentStatus.class))).thenReturn(mockInvoice);

        // Act
        PaymentResponse result = paymentService.handle(request);

        // Assert
        assertNotNull(result);
        verify(vnPayService).verify(request);
    }

    // ========== create(Invoice) Tests ==========

    @Test
    void create_Success_ReturnsNewPayment() {
        // Arrange
        Payment savedPayment = new Payment();
        savedPayment.setId(UUID.randomUUID());
        savedPayment.setInvoice(mockInvoice);
        savedPayment.setStatus(PaymentStatus.PENDING);
        savedPayment.setPrice(mockInvoice.getPrice());

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        Payment result = paymentService.create(mockInvoice);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(mockInvoice, capturedPayment.getInvoice());
        assertEquals(PaymentStatus.PENDING, capturedPayment.getStatus());
        assertEquals(mockInvoice.getPrice(), capturedPayment.getPrice());
        assertNotNull(capturedPayment.getCreateTime());
    }

    // ========== getPaymentUrl(Invoice) Tests ==========

    @Test
    void getPaymentUrl_Invoice_Success_ReturnsUrl() {
        // Arrange
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockInvoice);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(vnPayService).createPaymentUrl(mockPayment.getId(), mockPayment.getCreateTime(), mockPayment.getPrice());
    }

    @Test
    void getPaymentUrl_Invoice_ThrowsException_InvoiceAlreadyPaid() {
        // Arrange
        mockInvoice.setStatus(PaymentStatus.SUCCESS);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentUrl(mockInvoice));
        assertEquals("INVOICE_ALREADY_PAID", exception.getMessage());
    }

    @Test
    void getPaymentUrl_Invoice_ThrowsException_InvoiceCancel() {
        // Arrange
        mockInvoice.setStatus(PaymentStatus.CANCEL);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentUrl(mockInvoice));
        assertEquals("INVOICE_CANCEL", exception.getMessage());
    }

    @Test
    void getPaymentUrl_Invoice_ThrowsException_InvoiceExpired() {
        // Arrange
        mockInvoice.setExpireTime(LocalDateTime.now().minusMinutes(5));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(invoiceChangeService.update(any(Invoice.class), eq(PaymentStatus.CANCEL))).thenReturn(mockInvoice);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentUrl(mockInvoice));
        assertEquals("INVOICE_EXPIRE", exception.getMessage());

        // Verify cleanup operations
        verify(invoiceRepository).save(any(Invoice.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(invoiceChangeService).update(mockInvoice, PaymentStatus.CANCEL);
    }

    @Test
    void getPaymentUrl_Invoice_ExpiredWithNoPayment_HandlesGracefully() {
        // Arrange
        mockInvoice.setExpireTime(LocalDateTime.now().minusMinutes(5));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
        when(invoiceChangeService.update(any(Invoice.class), eq(PaymentStatus.CANCEL))).thenReturn(mockInvoice);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentUrl(mockInvoice));
        assertEquals("INVOICE_EXPIRE", exception.getMessage());

        // Verify only invoice operations, not payment
        verify(invoiceRepository).save(any(Invoice.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(invoiceChangeService).update(mockInvoice, PaymentStatus.CANCEL);
    }

    @Test
    void getPaymentUrl_Invoice_NoPayment_CreatesNewPayment() {
        // Arrange
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockInvoice);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentUrl_Invoice_PaymentExpired_CreatesNewPayment() {
        // Arrange
        mockPayment.setCreateTime(LocalDateTime.now().minusMinutes(15));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));

        Payment newPayment = new Payment();
        newPayment.setId(UUID.randomUUID());
        newPayment.setInvoice(mockInvoice);
        newPayment.setCreateTime(LocalDateTime.now());
        newPayment.setPrice(mockInvoice.getPrice());

        when(paymentRepository.save(any(Payment.class))).thenReturn(newPayment);
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockInvoice);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentUrl_Invoice_PaymentAtExactly10Minutes_CreatesNewPayment() {
        // Arrange
        mockPayment.setCreateTime(LocalDateTime.now().minusMinutes(10));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockInvoice);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentUrl_Invoice_PaymentWithin10Minutes_UsesExistingPayment() {
        // Arrange
        mockPayment.setCreateTime(LocalDateTime.now().minusMinutes(5));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockInvoice);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(vnPayService).createPaymentUrl(mockPayment.getId(), mockPayment.getCreateTime(), mockPayment.getPrice());
    }

    // ========== getPaymentUrl(UUID) Tests ==========

    @Test
    void getPaymentUrl_UUID_Success_ReturnsUrl() {
        // Arrange
        UUID invoiceId = mockInvoice.getId();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(mockInvoice));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(invoiceId);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void getPaymentUrl_UUID_ThrowsException_InvoiceNotFound() {
        // Arrange
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentUrl(invoiceId));
        assertEquals("INVOICE_NOT_FOUND", exception.getMessage());
    }

    // ========== getPaymentUrl(Payment) Tests ==========

    @Test
    void getPaymentUrl_Payment_Success_ReturnsUrl() {
        // Arrange
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockPayment);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(vnPayService).createPaymentUrl(mockPayment.getId(), mockPayment.getCreateTime(), mockPayment.getPrice());
    }

    @Test
    void getPaymentUrl_Payment_WithDifferentPrice_ReturnsUrl() {
        // Arrange
        mockPayment.setPrice(2000000L);
        when(vnPayService.createPaymentUrl(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn("http://vnpay.url");

        // Act
        String result = paymentService.getPaymentUrl(mockPayment);

        // Assert
        assertEquals("http://vnpay.url", result);
        verify(vnPayService).createPaymentUrl(mockPayment.getId(), mockPayment.getCreateTime(), 2000000L);
    }

    // ========== Edge Cases and Integration Tests ==========

    @Test
    void create_WithNullExpireTime_HandlesGracefully() {
        // Arrange
        mockInvoice.setExpireTime(null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        // Act
        Payment result = paymentService.create(mockInvoice);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void handle_VNPayResult_WithDifferentStatuses_MapsCorrectly() {
        // Arrange
        mockVNPayResult.setStatus(VNPayStatus.CANCEL);
        when(paymentRepository.findById(mockPayment.getId())).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(Payment.class), eq(PaymentResponse.class))).thenReturn(mockPaymentResponse);
        when(invoiceChangeService.update(any(Invoice.class), any(PaymentStatus.class))).thenReturn(mockInvoice);

        // Act
        PaymentResponse result = paymentService.handle(mockVNPayResult);

        // Assert
        assertNotNull(result);
        verify(invoiceChangeService).update(mockInvoice, PaymentStatus.CANCEL);
    }

    @Test
    void getPaymentUrl_Invoice_ConcurrentExpiry_HandlesCorrectly() {
        // Arrange
        mockInvoice.setExpireTime(LocalDateTime.now().minusSeconds(1));
        when(paymentRepository.findLatestByInvoice(mockInvoice)).thenReturn(Optional.of(mockPayment));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(invoiceChangeService.update(any(Invoice.class), eq(PaymentStatus.CANCEL))).thenReturn(mockInvoice);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentUrl(mockInvoice));
        assertEquals("INVOICE_EXPIRE", exception.getMessage());
    }
}