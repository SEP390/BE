package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.PaymentSlotRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentSlotService {
    private final PaymentService paymentService;
    private final RoomService roomService;
    private final SlotService slotService;
    private final PaymentSlotRepository paymentSlotRepository;
    private final ModelMapper modelMapper;

    public PaymentSlot create(User user, Slot slot, Semester semester) {
        PaymentSlot paymentSlot = new PaymentSlot();
        paymentSlot.setUser(user);
        paymentSlot.setSemester(semester);
        paymentSlot.setSlotId(slot.getId());
        paymentSlot.setSlotName(slot.getSlotName());
        paymentSlot.setPrice(slot.getRoom().getPricing().getPrice());
        paymentSlot.setRoomNumber(slot.getRoom().getRoomNumber());
        paymentSlot.setDormName(slot.getRoom().getDorm().getDormName());
        paymentSlot = paymentSlotRepository.save(paymentSlot);
        Payment payment = paymentService.create(user, slot);
        paymentSlot.setPayment(payment);
        return paymentSlot;
    }

    public Optional<PaymentSlot> getByPayment(Payment payment) {
        return paymentSlotRepository.findOne((r,q,c) -> {
            return c.equal(r.get("payment"), payment);
        });
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
        return new PagedModel<>(paymentSlotRepository.findAll(Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("payment").get("user"), user),
                (root, query, cb) -> cb.equal(root.get("payment").get("type"), PaymentType.BOOKING),
                status != null ? (root, query, cb) -> root.get("payment").get("status").in(status) : Specification.unrestricted()
        ), validPageable).map(p -> modelMapper.map(p, BookingHistoryResponse.class)));
    }

    public boolean hasPendingPayment(User user) {
        return paymentSlotRepository.exists(
                (r, q, c) -> c.and(
                        c.equal(r.get("payment").get("user"), user),
                        c.equal(r.get("payment").get("type"), PaymentType.BOOKING),
                        c.equal(r.get("payment").get("status"), PaymentStatus.PENDING)
                )
        );
    }

    public Payment getPendingPayment(User user) {
        var payments = paymentSlotRepository.findAll(
                (r, q, c) -> c.and(
                        c.equal(r.get("payment").get("user"), user),
                        c.equal(r.get("payment").get("type"), PaymentType.BOOKING),
                        c.equal(r.get("payment").get("status"), PaymentStatus.PENDING)
                ),
                PageRequest.of(0, 1)
        );
        return !payments.isEmpty() ? payments.getContent().getFirst().getPayment() : null;
    }

    public Payment getPendingPayment(User user, Slot slot) {
        var payments = paymentSlotRepository.findAll(
                (r, q, c) -> c.and(
                        c.equal(r.get("payment").get("user"), user),
                        c.equal(r.get("slotId"), slot.getId()),
                        c.equal(r.get("payment").get("type"), PaymentType.BOOKING),
                        c.equal(r.get("payment").get("status"), PaymentStatus.PENDING)
                ),
                PageRequest.of(0, 1)
        );
        return !payments.isEmpty() ? payments.getContent().getFirst().getPayment() : null;
    }

    @Transactional
    public void onPayment(Payment payment) {
        PaymentSlot paymentSlot = getByPayment(payment).orElseThrow();
        Slot slot = slotService.getById(paymentSlot.getSlotId()).orElse(null);
        // slot deleted/invalid
        if (slot == null) return;
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            roomService.successSlot(slot);
        } else {
            roomService.unlockSlot(slot);
        }
    }
}
