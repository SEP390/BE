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
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final VNPayService vNPayService;
    private final ModelMapper modelMapper;

    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment create(User user, Long price, PaymentType type) {
        return create(Payment.builder()
                .type(type)
                .status(PaymentStatus.PENDING)
                .createDate(LocalDateTime.now())
                .price(price)
                .user(user)
                .build());
    }

    /**
     * Tạo thanh toán cho hóa đơn điện nước
     *
     * @param bill hóa đơn
     * @return payment
     */
    public Payment create(User user, ElectricWaterBill bill) {
        return create(user, bill.getPrice(), PaymentType.ELECTRIC_WATER);
    }

    /**
     * Tạo thanh toán cho đặt phòng
     *
     * @param user user
     * @param slot slot
     * @return payment
     */
    public Payment create(User user, Slot slot) {
        return create(user, slot.getRoom().getPricing().getPrice(), PaymentType.BOOKING);
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

    public PaymentResponse toResponse(Payment payment) {
        return modelMapper.map(payment, PaymentResponse.class);
    }

    public boolean isExpire(Payment payment) {
        return ChronoUnit.MINUTES.between(payment.getCreateDate(), LocalDateTime.now()) >= 10;
    }

    public Payment expire(Payment payment) {
        payment.setStatus(PaymentStatus.CANCEL);
        return paymentRepository.save(payment);
    }
}
