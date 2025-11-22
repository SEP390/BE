package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.service.impl.SlotService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class GuardCheckinController {
    private final SlotService slotService;

    /**
     * checkin cho slot
     *
     * @param slotId id của slot
     */
    @PreAuthorize("hasRole('GUARD')")
    @PostMapping("/api/checkin")
    public BaseResponse<SlotResponseJoinRoomAndDormAndPricingAndUser> checkin(@RequestParam UUID slotId) {
        return new BaseResponse<>(slotService.checkin(slotId));
    }
}
