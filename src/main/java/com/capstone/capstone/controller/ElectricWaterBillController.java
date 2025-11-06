package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.electricwater.*;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterIndexResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterPricingResponse;
import com.capstone.capstone.dto.response.electricwater.UserElectricWaterResponse;
import com.capstone.capstone.service.impl.ElectricWaterService;
import com.capstone.capstone.service.impl.PaymentElectricWaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ElectricWaterBillController {
    private final ElectricWaterService electricWaterService;
    private final PaymentElectricWaterService paymentElectricWaterService;

    @PostMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> createIndex(@RequestBody @Valid CreateElectricWaterIndexRequest request) {
        return new BaseResponse<>(electricWaterService.createIndexResponse(request));
    }

    @GetMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> getIndex(
            @RequestParam UUID roomId,
            @RequestParam UUID semesterId) {
        return new BaseResponse<>(electricWaterService.getIndexResponse(roomId, semesterId));
    }

    @GetMapping("/api/electric-water-index/{id}/bill")
    public BaseResponse<ElectricWaterBillResponse> getIndexBill(@PathVariable UUID id) {
        return new BaseResponse<>(electricWaterService.getIndexBillResponse(id));
    }

    @PutMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> updateIndex(@RequestBody @Valid UpdateElectricWaterIndexRequest request) {
        return new BaseResponse<>(electricWaterService.updateIndexResponse(request));
    }

    @PostMapping("/api/electric-water-bill")
    public BaseResponse<ElectricWaterBillResponse> createBill(@RequestBody CreateElectricWaterBillRequest request) {
        return new BaseResponse<>(electricWaterService.createBillResponse(request));
    }

    @GetMapping("/api/electric-water-bill/user")
    public BaseResponse<PagedModel<UserElectricWaterResponse>> getUserBill(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(paymentElectricWaterService.getUserElectricWaterBills(pageable));
    }

    @GetMapping("/api/electric-water-bill/{id}/payment-url")
    public BaseResponse<String> getBillPayment(@PathVariable UUID id) {
        return new BaseResponse<>(paymentElectricWaterService.createPaymentUrl(id));
    }

    @GetMapping("/api/electric-water-pricing")
    public BaseResponse<List<ElectricWaterPricingResponse>> getAllPricing() {
        return new BaseResponse<>(electricWaterService.getAllPricing());
    }

    @GetMapping("/api/electric-water-pricing/{id}")
    public BaseResponse<ElectricWaterPricingResponse> getPricing(@PathVariable UUID id) {
        return new BaseResponse<>(electricWaterService.getPricing(id));
    }

    @GetMapping("/api/electric-water-pricing/current")
    public BaseResponse<ElectricWaterPricingResponse> getCurrentPricing() {
        return new BaseResponse<>(electricWaterService.getCurrentPricing());
    }

    @PostMapping("/api/electric-water-pricing")
    public BaseResponse<ElectricWaterPricingResponse> getPricing(@RequestBody @Valid CreateElectricWaterPricingRequest request) {
        return new BaseResponse<>(electricWaterService.createPricing(request));
    }

    @PutMapping("/api/electric-water-pricing")
    public BaseResponse<ElectricWaterPricingResponse> getPricing(@RequestBody @Valid UpdateElectricWaterPricingRequest request) {
        return new BaseResponse<>(electricWaterService.updatePricing(request));
    }
}
