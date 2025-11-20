package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
import com.capstone.capstone.service.impl.SlotHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SlotHistoryController {
    private final SlotHistoryService slotHistoryService;

    @GetMapping("/api/slots/history")
    public BaseResponse<PagedModel<SlotHistoryResponse>> getByCurrentUser(@PageableDefault Pageable pageable) {
        return new BaseResponse<>(slotHistoryService.getAllByCurrentUser(pageable));
    }
}
