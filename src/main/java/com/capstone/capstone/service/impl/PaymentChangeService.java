package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PaymentChangeService {
    private final PaymentRepository paymentRepository;
    private final InvoiceChangeService invoiceChangeService;
    private final VNPayService vnPayService;
    private final ModelMapper modelMapper;

    public Invoice handle(VNPayResult res) {
        var paymentId = res.getId();
        var payment = paymentRepository.findById(paymentId).orElseThrow();
        var invoice = payment.getInvoice();
        var vnPayStatus = res.getStatus();
        // chỉ cập nhật nếu đang PENDING (double render problem)
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            if (vnPayStatus == VNPayStatus.SUCCESS) {
                // thanh toán thành công
                invoice = invoiceChangeService.update(invoice, PaymentStatus.SUCCESS);
            } else {
                invoice = invoiceChangeService.update(invoice, PaymentStatus.CANCEL);
            }
        }
        return invoice;
    }

    @Transactional
    public InvoiceResponse handle(HttpServletRequest request) {
        var res = vnPayService.verify(request);
        return modelMapper.map(handle(res), InvoiceResponse.class);
    }

}
