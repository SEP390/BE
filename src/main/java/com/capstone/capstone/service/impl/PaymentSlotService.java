package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
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
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentSlotService {
    private final PaymentService paymentService;
    private final RoomService roomService;
    private final SlotService slotService;
    private final SemesterService semesterService;
    private final PaymentSlotRepository paymentSlotRepository;
    private final ModelMapper modelMapper;
    private final SlotHistoryService slotHistoryService;

    public PaymentSlot create(User user, Slot slot, Semester semester) {
        PaymentSlot paymentSlot = new PaymentSlot();
        paymentSlot.setUser(user);
        paymentSlot.setSemester(semester);
        paymentSlot.setSlotId(slot.getId());
        paymentSlot.setSlotName(slot.getSlotName());
        paymentSlot.setPrice(slot.getRoom().getPricing().getPrice());
        paymentSlot.setRoomNumber(slot.getRoom().getRoomNumber());
        paymentSlot.setDormName(slot.getRoom().getDorm().getDormName());
        Payment payment = paymentService.create(user, slot);
        paymentSlot.setPayment(payment);
        paymentSlot = paymentSlotRepository.save(paymentSlot);
        return paymentSlot;
    }

    public PaymentSlot create(User user, Slot slot) {
        Semester semester = semesterService.getNext();
        if (semester == null) throw new AppException("SEMESTER_NOT_FOUND");
        return create(user, slot, semester);
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
        var createDateDirection = Optional.ofNullable(pageable.getSort().getOrderFor("createDate")).map(Sort.Order::getDirection)
                .orElse(Sort.Direction.DESC);
        Sort validSort = Sort.by(createDateDirection, "payment.createDate");
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

    public PaymentSlot getPending(User user, Slot slot) {
        final UUID slotId = slot.getId();
        var payments = paymentSlotRepository.findAll(
                (r, q, c) -> c.and(
                        c.equal(r.get("payment").get("user"), user),
                        c.equal(r.get("slotId"), slotId),
                        c.equal(r.get("payment").get("type"), PaymentType.BOOKING),
                        c.equal(r.get("payment").get("status"), PaymentStatus.PENDING)
                ),
                PageRequest.of(0, 1)
        );
        PaymentSlot paymentSlot = !payments.isEmpty() ? payments.getContent().getFirst() : null;
        if (paymentSlot != null) {
            // unlock if expire
            if (paymentService.isExpire(paymentSlot.getPayment()) && slot.getStatus() == StatusSlotEnum.LOCK) {
                paymentSlot.setPayment(paymentService.expire(paymentSlot.getPayment()));
                slotService.unlock(slot);
            }
            return null;
        }
        return paymentSlot;
    }

    @Transactional
    public void onPayment(Payment payment) {
        PaymentSlot paymentSlot = getByPayment(payment).orElseThrow();
        Slot slot = slotService.getById(paymentSlot.getSlotId()).orElse(null);
        // slot deleted/invalid
        if (slot == null) return;
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            slot = roomService.successSlot(slot);
            slotHistoryService.create(payment.getUser(), paymentSlot.getSemester(), slot);
        } else {
            roomService.unlockSlot(slot);
        }
    }

    public String createPaymentUrl(User user, Slot slot) {
        PaymentSlot ps = create(user, slot);
        return paymentService.createPaymentUrl(ps.getPayment());
    }

    public String createPaymentUrl(Payment payment) {
        return paymentService.createPaymentUrl(payment);
    }
}
