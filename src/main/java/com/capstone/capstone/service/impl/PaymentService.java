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
import com.capstone.capstone.repository.ElectricWaterBillRepository;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
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
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final ElectricWaterBillRepository electricWaterBillRepository;

    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
    }

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

    public Payment create(ElectricWaterBill bill) {
        return create(Payment.builder()
                .type(PaymentType.ELECTRIC_WATER)
                .status(PaymentStatus.PENDING)
                .createDate(LocalDateTime.now())
                .price(bill.getPrice())
                .electricWaterBill(bill)
                .user(bill.getUser())
                .build());
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

    public void handleSuccess(Payment payment) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment = paymentRepository.save(payment);

        // success, change slot status to unavailable
        if (payment.getType() == PaymentType.BOOKING) {
            SlotHistory slotHistory = payment.getSlotHistory();
            Slot slot = slotHistory.getSlot();
            slotService.lockToUnavailable(slot);
        }
        if (payment.getType() == PaymentType.ELECTRIC_WATER) {
            ElectricWaterBill bill = payment.getElectricWaterBill();
            bill.setStatus(PaymentStatus.SUCCESS);
            electricWaterBillRepository.save(bill);
        }
    }

    public void handleFail(Payment payment) {
        payment.setStatus(PaymentStatus.CANCEL);
        payment = paymentRepository.save(payment);

        if (payment.getType() == PaymentType.BOOKING) {
            SlotHistory slotHistory = payment.getSlotHistory();
            Slot slot = slotHistory.getSlot();
            slotService.unlock(slot);
        }
    }

    public Payment handle(UUID paymentId, VNPayStatus status) {
        Payment payment = getById(paymentId);
        // invalid payment
        if (payment == null) {
            throw new AppException("PAYMENT_NOT_FOUND", paymentId);
        }
        // payment success
        if (payment.getStatus() == PaymentStatus.PENDING && status == VNPayStatus.SUCCESS) {
            handleSuccess(payment);
        }
        // payment fail
        if (payment.getStatus() == PaymentStatus.PENDING && status != VNPayStatus.SUCCESS) {
            handleFail(payment);
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

    public Page<BookingHistoryResponse> bookingHistory(User user, List<PaymentStatus> status, Pageable pageable) {
        Sort validSort = Sort.by(Optional.ofNullable(pageable.getSort().getOrderFor("createDate"))
                .orElse(Sort.Order.desc("createDate")));
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), 5, validSort);
        return paymentRepository.findAll(Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("user"), user),
                (root, query, cb) -> cb.equal(root.get("type"), PaymentType.BOOKING),
                status != null ? (root, query, cb) -> root.get("status").in(status) : Specification.unrestricted()
        ), validPageable).map(paymentMapper::toBookingHistoryResponse);
    }

    public PagedModel<PaymentResponse> history(List<PaymentStatus> status, Pageable pageable) {
        Sort validSort = Sort.by(Optional.ofNullable(pageable.getSort().getOrderFor("createDate"))
                .orElse(Sort.Order.desc("createDate")));
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), 5, validSort);
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        return new PagedModel<>(paymentRepository.findAll(Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("user"), user),
                status != null ? (root, query, cb) -> root.get("status").in(status) : Specification.unrestricted()
        ), validPageable).map(paymentMapper::toPaymentResponse));
    }

    public Payment latest(User user, Slot slot) {
        Payment example = new Payment();
        example.setUser(user);
        example.setType(PaymentType.BOOKING);
        SlotHistory slotHistoryExample = new SlotHistory();
        slotHistoryExample.setSlot(slot);
        example.setSlotHistory(slotHistoryExample);
        return paymentRepository.findOne(Example.of(example)).orElse(null);
    }
}
