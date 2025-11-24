package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponseJoinUser;
import com.capstone.capstone.service.impl.SlotHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
public class SlotHistoryController {
    private final SlotHistoryService slotHistoryService;

    @GetMapping("/api/user/slot-history")
    public BaseResponse<PagedModel<SlotHistoryResponse>> getByCurrentUser(@PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        return new BaseResponse<>(slotHistoryService.getAllByCurrentUser(filter, pageable));
    }

    @GetMapping("/api/slot-history")
    public BaseResponse<PagedModel<SlotHistoryResponseJoinUser>> getAll(
            @PageableDefault Pageable pageable
    ) {
        Map<String, Object> filter = new HashMap<>();
        return new BaseResponse<>(slotHistoryService.getAll(filter, pageable));
    }
}
