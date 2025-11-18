package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.impl.SlotService;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class GuardCheckinController {
    private final SlotService slotService;
    private final UserRepository userRepository;

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

    /**
     * list all userCode
     *
     * @param userCode search by text
     */
    @PreAuthorize("hasRole('GUARD') or hasRole('MANAGER')")
    @PostMapping("/api/users/userCode")
    public BaseResponse<List<String>> getUserCodes(@RequestParam(required = false) String userCode) {
        return new BaseResponse<>(userRepository.findAll(userCode == null || userCode.isBlank() ? Specification.unrestricted() : (r, q, c) -> {
            return c.like(r.get("userCode"), "%" + userCode.replace("%", "") + "%");
        }).stream().map(User::getUserCode).toList());
    }
}
