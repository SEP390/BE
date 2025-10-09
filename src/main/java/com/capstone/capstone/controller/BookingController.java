package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.booking.BookingRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.booking.SlotBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotHistoryResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.BookingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/api/booking/create")
    public BaseResponse<SlotBookingResponse> create(@RequestBody BookingRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new BaseResponse<>(200, "success", bookingService.create(user, request.getId()));
    }

    @GetMapping("/api/booking/history")
    public BaseResponse<List<SlotHistoryResponse>> history(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new BaseResponse<>(200, "success", bookingService.history(user));
    }
}
