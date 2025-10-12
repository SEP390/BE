package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.PaymentResponse;
import com.capstone.capstone.dto.response.booking.PaymentVerifyResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.mapper.PaymentMapper;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final VNPayService vNPayService;
    private final SlotService slotService;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final SemesterService semesterService;

    /**
     * Create payment for slot
     */
    public Payment createForSlot(User user, SlotHistory slotHistory) {
        long price = slotHistory.getPrice();
        LocalDateTime createDate = LocalDateTime.now();
        PaymentStatus status =  PaymentStatus.PENDING;
        PaymentType type = PaymentType.BOOKING;

        Payment payment = new Payment();
        payment.setStatus(status);
        payment.setUser(user);
        payment.setPrice(price);
        payment.setCreateDate(createDate);
        payment.setSlotHistory(slotHistory);
        payment.setType(type);
        payment = paymentRepository.save(payment);

        return payment;
    }

    /**
     * Create payment url for invoice
     */
    public String createPaymentUrl(Payment payment) {
        return vNPayService.createPaymentUrl(payment.getId(), payment.getCreateDate(), payment.getPrice());
    }

    public Payment getById(UUID id) {
        return paymentRepository.findById(id).orElseThrow();
    }

    @Transactional
    public PaymentVerifyResponse verify(HttpServletRequest request, User user) {
        // vnpay verify hash
        var result = vNPayService.verify(request);

        Payment payment = getById(result.getId());

        // invalid payment
        if (payment == null) {
            throw new AppException("PAYMENT_NOT_FOUND", result.getId());
        }

        SlotHistory slotHistory = payment.getSlotHistory();
        Slot slot = slotHistory.getSlot();

        // unauthorized
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new AppException("UNAUTHORIZED");
        }

        // payment success
        if (payment.getStatus() == PaymentStatus.PENDING && result.getStatus() == VNPayStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment = paymentRepository.save(payment);

            // success, change slot status to unavailable
            if (payment.getType() == PaymentType.BOOKING) {
                slotService.lockToUnavailable(slot);
            }
        }

        // payment fail
        if (payment.getStatus() == PaymentStatus.PENDING && result.getStatus() != VNPayStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.CANCEL);
            payment = paymentRepository.save(payment);

            if (payment.getType() == PaymentType.BOOKING) {
                slotService.unlock(slot);
            }
        }
        return PaymentVerifyResponse.builder()
                .status(result.getStatus())
                .price(payment.getPrice())
                .build();
    }

    public Page<BookingHistoryResponse> bookingHistory(User user, PaymentStatus status, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        Payment example = new Payment();
        example.setStatus(status);
        example.setUser(user);
        example.setType(PaymentType.BOOKING);
        return paymentRepository.findAll(Example.of(example), pageable).map(paymentMapper::toBookingHistoryResponse);
    }

    public Page<PaymentResponse> history(int page) {
        Pageable pageable = PageRequest.of(page, 5);
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Payment example = new Payment();
        example.setUser(user);
        return paymentRepository.findAll(Example.of(example), pageable).map(paymentMapper::toPaymentResponse);
    }

    public BookingHistoryResponse currentBooking() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Semester semester = semesterService.getNextSemester();
        return paymentMapper.toBookingHistoryResponse(paymentRepository.findCurrentBooking(user, semester));
    }
}
