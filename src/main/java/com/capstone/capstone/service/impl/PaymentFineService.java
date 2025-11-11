package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.entity.FineBill;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.PaymentFine;
import com.capstone.capstone.repository.PaymentFineRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentFineService {
    private final PaymentFineRepository paymentFineRepository;
    private final FineBillService fineBillService;

    public Optional<PaymentFine> getByPayment(Payment payment) {
        return paymentFineRepository.findOne((r,q,c) -> {
            return c.equal(r.get("payment"), payment);
        });
    }

    public void onPayment(Payment payment) {
        PaymentFine paymentFine = getByPayment(payment).orElseThrow();
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            FineBill bill = fineBillService.setPaid(paymentFine.getBill());
        }
    }
}
