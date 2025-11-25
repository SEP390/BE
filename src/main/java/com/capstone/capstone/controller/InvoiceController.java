package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final InvoiceChangeService invoiceChangeService;
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/api/invoices")
    public BaseResponse<PagedModel<InvoiceResponseJoinUser>> getAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) InvoiceType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("userId", userId);
        filter.put("type", type);
        filter.put("status", status);
        filter.put("startDate", startDate);
        filter.put("endDate", endDate);
        return new BaseResponse<>(invoiceService.getAll(filter, pageable));
    }

    @PreAuthorize("hasRole('MANAGER') or @invoiceService.authorize(#id)")
    @GetMapping("/api/invoices/{id}/payment")
    public BaseResponse<String> getPaymentUrl(@PathVariable("id") UUID id) {
        return new BaseResponse<>(paymentService.getPaymentUrl(id));
    }

    @GetMapping("/api/user/invoices")
    public BaseResponse<PagedModel<InvoiceResponse>> getAllByUser(
            @RequestParam(required = false) InvoiceType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("type", type);
        filter.put("status", status);
        filter.put("startDate", startDate);
        filter.put("endDate", endDate);
        return new BaseResponse<>(invoiceService.getAllByUser(filter, pageable));
    }

    @GetMapping("/api/user/invoices/count")
    public BaseResponse<InvoiceCountResponse> getUserCount() {
        return new BaseResponse<>(invoiceService.userCount());
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
    public BaseResponse<InvoiceResponseJoinUser> update(@PathVariable UUID id, @Valid @RequestBody UpdateInvoiceRequest request) {
        return new BaseResponse<>(invoiceChangeService.update(id, request));
    }
}
