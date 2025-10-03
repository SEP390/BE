package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.request.booking.SlotBookingRequest;
import com.capstone.capstone.dto.response.booking.SlotBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotHistoryResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/api/booking/create")
    public BaseResponse<SlotBookingResponse> createBooking(@RequestBody SlotBookingRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new BaseResponse<>(200, "success", bookingService.createBooking(user.getId(), request.getId()));
    }

    @GetMapping("/api/booking/current")
    public BaseResponse<SlotHistoryResponse> getCurrentBooking(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new BaseResponse<>(200, "success", bookingService.getCurrentBooking(user.getId()));
    }

    @GetMapping("/api/booking/result")
    public BaseResponse<?> handlePaymentReturn(HttpServletRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new BaseResponse<>(200, "success", bookingService.handlePaymentResult(request));
    }
}
