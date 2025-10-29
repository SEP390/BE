package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.PaymentElectricWaterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentElectricWaterService {
    private final PaymentService paymentService;
    private final ElectricWaterService electricWaterService;
    private final PaymentElectricWaterRepository paymentElectricWaterRepository;

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
}
