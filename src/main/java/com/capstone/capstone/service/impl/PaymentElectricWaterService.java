package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.electricwater.UserElectricWaterResponse;
import com.capstone.capstone.dto.response.payment.PaymentCoreResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.PaymentElectricWaterRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentElectricWaterService {
    private final RoomService roomService;
    private final ElectricWaterService electricWaterService;
    private final PaymentElectricWaterRepository paymentElectricWaterRepository;
    private final ModelMapper modelMapper;

    public Optional<PaymentElectricWater> getByPayment(Payment payment) {
        return paymentElectricWaterRepository.findOne((r,q,c) -> {
            return c.equal(r.get("payment"), payment);
        });
    }

    public List<PaymentElectricWater> getAllByBillAndSuccess(ElectricWaterBill bill) {
        return paymentElectricWaterRepository.findAll((r,q,c) -> {
            return c.and(
                    c.equal(r.get("bill"), bill),
                    c.equal(r.get("payment").get("status"), PaymentStatus.SUCCESS)
            );
        });
    }

    @Transactional
    public void onPayment(Payment payment) {
        PaymentElectricWater paymentElectricWater = getByPayment(payment).orElseThrow();
        var bill = paymentElectricWater.getBill();
        var userCount = bill.getUserCount();
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            var paidCount = getAllByBillAndSuccess(bill).size();
            if (paidCount == userCount) {
                electricWaterService.successBill(bill);
            }
        }
    }

    public List<PaymentElectricWater> getAllByBillAndUserAndStatus(ElectricWaterBill bill, User user, PaymentStatus status) {
        return paymentElectricWaterRepository.findAll(Specification.allOf(
                (r, q, c) -> c.equal(r.get("bill"), bill),
                (user != null) ? (r, q, c) -> c.equal(r.get("payment").get("user"), user) : Specification.unrestricted(),
                (status != null) ? (r,q,c) -> c.equal(r.get("payment").get("status"), status) : Specification.unrestricted()
        ));
    }

    /**
     * 1 bill, 1 user -> chỉ 1/0 payment success
     * @param bill bill
     * @param user user
     * @return payment
     */
    public Optional<PaymentElectricWater> getByBillAndUserAndSuccess(ElectricWaterBill bill, User user) {
        var payments = getAllByBillAndUserAndStatus(bill, user, PaymentStatus.SUCCESS);
        if (payments.isEmpty()) return Optional.empty();
        return Optional.of(payments.getFirst());
    }

    /**
     * Lấy thông tin hóa đơn điện nước của phòng
     * @return thông tin hóa đơn điện nước
     */
    public PagedModel<UserElectricWaterResponse> getUserElectricWaterBills(Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        Room room = roomService.getByUser(user).orElseThrow();
        Page<ElectricWaterBill> bills = electricWaterService.getBillsOfRoom(room, pageable);
        return new PagedModel<>(bills.map(bill -> {
            var response = modelMapper.map(bill, UserElectricWaterResponse.class);
            getByBillAndUserAndSuccess(bill, user).map(PaymentElectricWater::getPayment).ifPresent(payment -> response.setPayment(modelMapper.map(payment, PaymentCoreResponse.class)));
            return response;
        }));
    }
}
