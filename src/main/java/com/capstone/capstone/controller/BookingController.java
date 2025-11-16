package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    /**
     * Create booking and return payment url
     */
    @PostMapping("/api/booking")
    public BaseResponse<String> create(@RequestParam UUID slotId) {
        return new BaseResponse<>(bookingService.create(slotId));
    }
}
