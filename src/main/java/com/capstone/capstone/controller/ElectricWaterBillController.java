package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.electricwater.ElectricWaterBillRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.ElectricWaterBillService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class ElectricWaterBillController {
    private final ElectricWaterBillService electricWaterBillService;

    @PostMapping("/api/electric-water-room")
    public BaseResponse<?> createRoomBill(@RequestBody ElectricWaterBillRequest request) {
        return new BaseResponse<>(200, "success", electricWaterBillService.create(request));
    }

    @GetMapping("/api/electric-water-room/{id}")
    public BaseResponse<?> getRoomBill(@PathVariable UUID id) {
        return new BaseResponse<>(200, "success", electricWaterBillService.getByRoomId(id));
    }

    @GetMapping("/api/electric-water")
    public BaseResponse<?> getBill() {
        return new BaseResponse<>(200, "success", electricWaterBillService.getCurrent());
    }

    @GetMapping("/api/electric-water/{id}")
    public BaseResponse<?> getBillPayment(@PathVariable UUID id) {
        return new BaseResponse<>(200, "success", electricWaterBillService.getBillPayment(id));
    }
}
