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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SlotHistoryController {
    private final SlotHistoryService slotHistoryService;

    @GetMapping("/api/user/slot-history")
    public BaseResponse<PagedModel<SlotHistoryResponse>> getByCurrentUser(
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) UUID roomId,
            @PageableDefault Pageable pageable
    ) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("semesterId", semesterId);
        filter.put("roomId", roomId);
        return new BaseResponse<>(slotHistoryService.getAllByCurrentUser(filter, pageable));
    }

    @GetMapping("/api/user/slot-history/current")
    public BaseResponse<SlotHistoryResponse> getCurrent() {
        return new BaseResponse<>(slotHistoryService.getCurrent());
    }

    @GetMapping("/api/slot-history")
    public BaseResponse<PagedModel<SlotHistoryResponseJoinUser>> getAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID semesterId,
            @PageableDefault Pageable pageable
    ) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("semesterId", semesterId);
        filter.put("roomId", roomId);
        filter.put("userId", userId);
        return new BaseResponse<>(slotHistoryService.getAll(filter, pageable));
    }
}
