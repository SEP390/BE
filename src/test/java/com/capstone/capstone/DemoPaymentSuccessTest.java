package com.capstone.capstone;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.service.impl.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class DemoPaymentSuccessTest {
    @Test
    void paymentSuccess(@Autowired InvoiceRepository invoiceRepository, @Autowired PaymentService paymentService) {
        Invoice invoice = invoiceRepository.findOne((r, q, c) -> {
            return c.equal(r.get("status"), PaymentStatus.PENDING);
        }).orElseThrow();
        VNPayResult result = new VNPayResult();
        result.setId(invoice.getId());
        result.setStatus(VNPayStatus.SUCCESS);
        paymentService.handle(result);
    }
}
