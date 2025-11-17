package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.service.impl.InvoiceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/api/invoices")
    public BaseResponse<PagedModel<InvoiceResponse>> getAll(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(invoiceService.getAll(pageable));
    }

    @PostMapping("/api/invoices")
    public BaseResponse<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return new BaseResponse<>(invoiceService.create(request));
    }
}
