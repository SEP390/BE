package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.payment.PaymentResponse;
import com.capstone.capstone.dto.response.payment.PaymentVerifyResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SortUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final VNPayService vNPayService;
    private final ModelMapper modelMapper;
    private final SlotHistoryService slotHistoryService;

    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Create payment for slot history
     *
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
     * Tạo thanh toán cho hóa đơn điện nước
     *
     * @param bill hóa đơn
     * @return payment
     */
    public Payment create(User user, ElectricWaterBill bill) {
        return create(Payment.builder()
                .type(PaymentType.ELECTRIC_WATER)
                .status(PaymentStatus.PENDING)
                .createDate(LocalDateTime.now())
                .electricWaterBill(bill)
                .price(bill.getPrice())
                .user(user)
                .build());
    }

    /**
     * Tạo thanh toán cho đặt phòng
     *
     * @param user user
     * @param slot slot
     * @return payment
     */
    public Payment create(User user, Slot slot) {
        return create(Payment.builder()
                .type(PaymentType.BOOKING)
                .status(PaymentStatus.PENDING)
                .createDate(LocalDateTime.now())
                .slotHistory(slotHistoryService.create(user, slot))
                .price(slot.getRoom().getPricing().getPrice())
                .user(user)
                .build());
    }

    /**
     * Create payment url for invoice
     *
     * @param payment payment
     * @return payment url
     */
    public String createPaymentUrl(Payment payment) {
        return vNPayService.createPaymentUrl(payment.getId(), payment.getCreateDate(), payment.getPrice());
    }

    public String createPaymentUrl(User user, ElectricWaterBill bill) {
        Payment payment = create(user, bill);
        return vNPayService.createPaymentUrl(payment.getId(), payment.getCreateDate(), payment.getPrice());
    }

    public Payment getById(UUID id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public Payment updateStatus(Payment payment, VNPayStatus status) {
        if (status == VNPayStatus.SUCCESS) payment.setStatus(PaymentStatus.SUCCESS);
        else payment.setStatus(PaymentStatus.CANCEL);
        return paymentRepository.save(payment);
    }

    /**
     * Xác minh thanh toán
     *
     * @param request http request
     * @return kết quả xác minh
     */
    @Transactional
    public PaymentVerifyResponse verify(HttpServletRequest request) {
        // vnpay verify hash
        var result = vNPayService.verify(request);
        Payment payment = getById(result.getId());
        // invalid payment
        if (payment == null) {
            throw new AppException("PAYMENT_NOT_FOUND", result.getId());
        }
        boolean update = false;
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment = updateStatus(payment, result.getStatus());
            update = true;
        }
        return PaymentVerifyResponse.builder()
                .update(update)
                .status(result.getStatus())
                .payment(payment)
                .build();
    }

    /**
     * Get booking history of user
     *
     * @param user     user
     * @param status   status of payment
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
     * Lịch sử thanh toán của user hiện tại
     *
     * @param status   trạng thái thanh toán
     * @param pageable page, size, sort by createDate
     * @return lịch sủ thanh toán
     */
    public PagedModel<PaymentResponse> history(PaymentStatus status, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        Sort validSort = SortUtil.getSort(pageable, "createDate", "price");
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 99), validSort);
        return new PagedModel<>(paymentRepository.findAll(Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("user"), user),
                status != null ? (root, query, cb) -> cb.equal(root.get("status"), status) : Specification.unrestricted()
        ), validPageable).map(p -> modelMapper.map(p, PaymentResponse.class)));
    }

    /**
     * Lấy tất cả thanh toán của hóa đơn điện nước
     *
     * @param bill hóa đơn điện nước
     * @param status lọc theo status
     * @return tất cả thanh toán
     */
    public List<Payment> getAllByElectricWaterBill(ElectricWaterBill bill, User user, PaymentStatus status) {
        return paymentRepository.findAll(Specification.allOf(
                (r, q, c) -> c.equal(r.get("electricWaterBill"), bill),
                (user != null) ? (r, q, c) -> c.equal(r.get("user"), user) : Specification.unrestricted(),
                (status != null) ? (r,q,c) -> c.equal(r.get("status"), status) : Specification.unrestricted()
        ));
    }

    public PaymentResponse toResponse(Payment payment) {
        return modelMapper.map(payment, PaymentResponse.class);
    }

    public Payment getLatestPendingBookingByUser(User user) {
        var payments = paymentRepository.findAll(
                (r, q, c) -> c.and(
                        c.equal(r.get("user"), user),
                        c.equal(r.get("type"), PaymentType.BOOKING),
                        c.equal(r.get("status"), PaymentStatus.PENDING)
                ),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"))
        );
        return !payments.isEmpty() ? payments.getContent().getFirst() : null;
    }

    public Payment getLatestPendingBookingByUserAndSlot(User user, Slot slot) {
        var payments = paymentRepository.findAll(
                (r, q, c) -> c.and(
                        c.equal(r.get("user"), user),
                        c.equal(r.get("slotHistory").get("slotId"), slot.getId()),
                        c.equal(r.get("type"), PaymentType.BOOKING),
                        c.equal(r.get("status"), PaymentStatus.PENDING)
                ),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"))
        );
        return !payments.isEmpty() ? payments.getContent().getFirst() : null;
    }

    public boolean isExpire(Payment payment) {
        return ChronoUnit.MINUTES.between(payment.getCreateDate(), LocalDateTime.now()) >= 10;
    }

    public Payment expire(Payment payment) {
        payment.setStatus(PaymentStatus.CANCEL);
        return paymentRepository.save(payment);
    }

    public boolean hasBooking(User user, Slot slot) {
        return paymentRepository.exists(
                (r, q, c) -> c.and(
                        c.equal(r.get("user"), user),
                        c.equal(r.get("type"), PaymentType.BOOKING),
                        c.equal(r.get("slotHistory").get("slotId"), slot.getId()),
                        c.equal(r.get("status"), PaymentStatus.PENDING)
                )
        );
    }
}
