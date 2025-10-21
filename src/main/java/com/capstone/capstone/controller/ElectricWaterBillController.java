package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.electricwater.*;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterIndexResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterPricingResponse;
import com.capstone.capstone.service.impl.ElectricWaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ElectricWaterBillController {
    private final ElectricWaterService electricWaterService;

    @PostMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> createIndex(@RequestBody @Valid CreateElectricWaterIndexRequest request) {
        return new BaseResponse<>(electricWaterService.createIndexResponse(request));
    }

    @GetMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> getIndex(@RequestParam UUID roomId) {
        return new BaseResponse<>(electricWaterService.getIndexResponseOfRoom(roomId));
    }

    @PutMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> updateIndex(@RequestBody @Valid UpdateElectricWaterIndexRequest request) {
        return new BaseResponse<>(electricWaterService.updateIndexResponse(request));
    }

    @PostMapping("/api/electric-water-bill")
    public BaseResponse<ElectricWaterBillResponse> createBill(@RequestBody CreateElectricWaterBillRequest request) {
        return new BaseResponse<>(electricWaterService.createBillResponse(request));
    }

    @GetMapping("/api/electric-water-bill")
    public BaseResponse<ElectricWaterBillResponse> getBill() {
        return new BaseResponse<>(electricWaterService.getCurrentBillResponse());
    }

    @GetMapping("/api/electric-water-bill/{id}/payment-url")
    public BaseResponse<String> getBillPayment(@PathVariable UUID id) {
        return new BaseResponse<>(electricWaterService.createPaymentUrl(id));
    }

    @GetMapping("/api/electric-water-pricing")
    public BaseResponse<List<ElectricWaterPricingResponse>> getAllPricing() {
        return new BaseResponse<>(electricWaterService.getAllPricing());
    }

    @GetMapping("/api/electric-water-pricing/{id}")
    public BaseResponse<ElectricWaterPricingResponse> getPricing(@PathVariable UUID id) {
        return new BaseResponse<>(electricWaterService.getPricing(id));
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
