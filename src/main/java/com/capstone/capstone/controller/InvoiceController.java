package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.service.impl.InvoiceService;
import com.capstone.capstone.service.impl.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @GetMapping("/api/invoices")
    public BaseResponse<PagedModel<InvoiceResponseJoinUser>> getAll(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(invoiceService.getAll(pageable));
    }

    @GetMapping("/api/user/invoices")
    public BaseResponse<PagedModel<InvoiceResponse>> getAllByUser(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(invoiceService.getAllByUser(pageable));
    }

    @GetMapping("/api/invoices/count")
    public BaseResponse<InvoiceCountResponse> getCount() {
        return new BaseResponse<>(invoiceService.count());
    }

    @PostMapping("/api/invoices")
    public BaseResponse<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return new BaseResponse<>(invoiceService.create(request));
    }

    @PostMapping("/api/invoices/{id}")
    public BaseResponse<InvoiceResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateInvoiceRequest request) {
        VNPayResult res = new VNPayResult();
        res.setId(id);
        res.setStatus(request.getStatus());
        var invoice = paymentService.handle(res);
        return new BaseResponse<>(paymentService.toResponse(invoice));
    }
}
