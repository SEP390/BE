package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.service.impl.SlotService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SlotController {
    private final SlotService slotService;

    @GetMapping("/api/slots/current")
    public BaseResponse<SlotResponseJoinRoomAndDormAndPricing> getCurrent() {
        return new BaseResponse<>(slotService.getCurrent());
    }

    @GetMapping("/api/slots/{id}")
    public BaseResponse<SlotResponseJoinRoomAndDormAndPricingAndUser> getById(@PathVariable UUID id) {
        return new BaseResponse<>(slotService.getResponseById(id));
    }

    /**
     * @param userCode lọc theo mã sinh viên
     * @param pageable phân trang/sort
     * @return danh sách các slot chờ checkin
     */
    @GetMapping("/api/slots")
    public BaseResponse<PagedModel<SlotResponseJoinRoomAndDormAndPricingAndUser>> getAll(
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) StatusSlotEnum status,
            @PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("userCode", userCode);
        filter.put("userId", userId);
        filter.put("status", status);
        return new BaseResponse<>(slotService.getAll(filter, pageable));
    }


    /**
     * [Guard] checkin cho slot
     * @param id id của slot
     */
    @PostMapping("/api/slots/checkout/{id}")
    public BaseResponse<SlotResponseJoinRoomAndDormAndPricingAndUser> checkout(@PathVariable UUID id) {
        return new BaseResponse<>(slotService.checkout(id));
    }
}
