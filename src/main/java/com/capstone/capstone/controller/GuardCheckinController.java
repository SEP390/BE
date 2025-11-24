package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.checkin.GuardCheckinRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.service.impl.SlotService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class GuardCheckinController {
    private final SlotService slotService;

    /**
     * checkin cho slot
     */
    @PreAuthorize("hasRole('GUARD')")
    @PostMapping("/api/checkin")
    public BaseResponse<SlotResponseJoinRoomAndDormAndPricingAndUser> checkin(@RequestBody @Valid GuardCheckinRequest request) {
        return new BaseResponse<>(slotService.checkin(request));
    }
}
