package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterIndexRequest;
import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterPricingRequest;
import com.capstone.capstone.dto.request.electricwater.UpdateElectricWaterPricingRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterIndexResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterPricingResponse;
import com.capstone.capstone.service.impl.ElectricWaterService;
import com.capstone.capstone.service.impl.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ElectricWaterBillController {
    private final ElectricWaterService electricWaterService;
    private final PaymentService paymentService;

    @PostMapping("/api/electric-water-index")
    public BaseResponse<ElectricWaterIndexResponse> createIndex(@RequestBody CreateElectricWaterIndexRequest request) {
        return new BaseResponse<>(electricWaterService.createIndex(request));
    }

    @GetMapping("/api/electric-water-index/{roomId}")
    public BaseResponse<ElectricWaterIndexResponse> getIndex(@PathVariable UUID roomId) {
        return new BaseResponse<>(electricWaterService.getIndexResponseOfRoom(roomId));
    }

    @GetMapping("/api/electric-water-bill")
    public BaseResponse<ElectricWaterBillResponse> getBill() {
        return new BaseResponse<>(electricWaterService.getCurrentBillResponse());
    }

    @GetMapping("/api/electric-water-bill/{id}")
    public BaseResponse<String> getBillPayment(@PathVariable UUID id) {
        return new BaseResponse<>(paymentService.createElectricWaterBillPaymentUrl(id));
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
    public BaseResponse<ElectricWaterPricingResponse> getPricing(@RequestBody CreateElectricWaterPricingRequest request) {
        return new BaseResponse<>(electricWaterService.createPricing(request));
    }

    @PutMapping("/api/electric-water-pricing")
    public BaseResponse<ElectricWaterPricingResponse> getPricing(@RequestBody UpdateElectricWaterPricingRequest request) {
        return new BaseResponse<>(electricWaterService.updatePricing(request));
    }
}
