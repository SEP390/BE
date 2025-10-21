package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.PaymentResponse;
import com.capstone.capstone.dto.response.booking.PaymentVerifyResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.ElectricWaterBillRepository;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final VNPayService vNPayService;
    private final SlotService slotService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ElectricWaterBillService electricWaterBillService;

    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Create payment for slot history
     * @param slotHistory slot history
     * @return payment
     */
    public Payment create(SlotHistory slotHistory) {
        return create(Payment.builder()
                .type(PaymentType.BOOKING)
                .status(PaymentStatus.PENDING)
                .createDate(LocalDateTime.now())
                .price(slotHistory.getPrice())
                .slotHistory(slotHistory)
                .user(slotHistory.getUser())
                .build());
    }

    /**
     * Create payment for electric water bill
     * @param bill electric water bill
     * @return payment
     */
    public Payment create(ElectricWaterBill bill) {
        return create(Payment.builder()
                .type(PaymentType.ELECTRIC_WATER)
                .status(PaymentStatus.PENDING)
                .createDate(LocalDateTime.now())
                .electricWaterBill(bill)
                .price(bill.getPrice())
                .user(bill.getUser())
                .build());
    }

    /**
     * Create payment url for invoice
     * @param payment payment
     * @return payment url
     */
    public String createPaymentUrl(Payment payment) {
        return vNPayService.createPaymentUrl(payment.getId(), payment.getCreateDate(), payment.getPrice());
    }

    public Payment getById(UUID id) {
        return paymentRepository.findById(id).orElseThrow();
    }

    public Payment handle(UUID paymentId, VNPayStatus status) {
        Payment payment = getById(paymentId);
        // invalid payment
        if (payment == null) {
            throw new AppException("PAYMENT_NOT_FOUND", paymentId);
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            if (status == VNPayStatus.SUCCESS) payment.setStatus(PaymentStatus.SUCCESS);
            else payment.setStatus(PaymentStatus.CANCEL);
            payment = paymentRepository.save(payment);
            if (payment.getType() == PaymentType.BOOKING)
                slotService.onPayment(payment.getSlotHistory().getSlot(), status);
            if (payment.getType() == PaymentType.ELECTRIC_WATER)
                electricWaterBillService.onPayment(payment.getElectricWaterBill(), status);
        }
        return payment;
    }

    @Transactional
    public PaymentVerifyResponse verify(HttpServletRequest request) {
        // vnpay verify hash
        var result = vNPayService.verify(request);

        Payment payment = handle(result.getId(), result.getStatus());

        return PaymentVerifyResponse.builder()
                .status(result.getStatus())
                .price(payment.getPrice())
                .build();
    }

    /**
     * Get booking history of user
     * @param user user
     * @param status status of payment
     * @param pageable pageable
     * @return history
     */
    public PagedModel<BookingHistoryResponse> getBookingHistory(User user, List<PaymentStatus> status, Pageable pageable) {
        Sort validSort = Sort.by(Optional.ofNullable(pageable.getSort().getOrderFor("createDate"))
                .orElse(Sort.Order.desc("createDate")));
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), 5, validSort);
        return new PagedModel<>(paymentRepository.findAll(Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("user"), user),
                (root, query, cb) -> cb.equal(root.get("type"), PaymentType.BOOKING),
                status != null ? (root, query, cb) -> root.get("status").in(status) : Specification.unrestricted()
        ), validPageable).map(p -> modelMapper.map(p, BookingHistoryResponse.class)));
    }

    /**
     * Get payment history of current user
     * @param status payment status
     * @param pageable pageable
     * @return payment history of current user
     */
    public PagedModel<PaymentResponse> history(List<PaymentStatus> status, Pageable pageable) {
        Sort validSort = Sort.by(Optional.ofNullable(pageable.getSort().getOrderFor("createDate"))
                .orElse(Sort.Order.desc("createDate")));
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), 5, validSort);
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        return new PagedModel<>(paymentRepository.findAll(Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("user"), user),
                status != null ? (root, query, cb) -> root.get("status").in(status) : Specification.unrestricted()
        ), validPageable).map(p -> modelMapper.map(p, PaymentResponse.class)));
    }

    public String createElectricWaterBillPaymentUrl(UUID id) {
        ElectricWaterBill bill = electricWaterBillService.getById(id);
        Payment payment = create(bill);
        return createPaymentUrl(payment);
    }
}
