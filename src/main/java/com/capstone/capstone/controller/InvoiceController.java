package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.service.impl.InvoiceChangeService;
import com.capstone.capstone.service.impl.InvoiceService;
import com.capstone.capstone.service.impl.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final InvoiceChangeService invoiceChangeService;
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/api/invoices")
    public BaseResponse<PagedModel<InvoiceResponseJoinUser>> getAll(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(invoiceService.getAll(pageable));
    }

    @PreAuthorize("hasRole('MANAGER') or @invoiceService.authorize(#id)")
    @GetMapping("/api/invoices/{id}/payment")
    public BaseResponse<String> getPaymentUrl(@PathVariable("id") UUID id) {
        return new BaseResponse<>(paymentService.getPaymentUrl(id));
    }

    @GetMapping("/api/user/invoices")
    public BaseResponse<PagedModel<InvoiceResponse>> getAllByUser(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(invoiceService.getAllByUser(pageable));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/api/invoices/count")
    public BaseResponse<InvoiceCountResponse> getCount() {
        return new BaseResponse<>(invoiceService.count());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/api/invoices")
    public BaseResponse<?> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return new BaseResponse<>(invoiceService.create(request));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/api/invoices/{id}")
    public BaseResponse<InvoiceResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateInvoiceRequest request) {
        return new BaseResponse<>(invoiceChangeService.update(id, request));
    }
}
