package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.BookingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/api/booking/create")
    public BaseResponse<CreateBookingResponse> create(@RequestBody CreateBookingRequest request) {
        return new BaseResponse<>(200, "success", bookingService.create(request));
    }

    @GetMapping("/api/booking/current")
    public BaseResponse<?> current() {
        return new BaseResponse<>(200, "success", bookingService.current());
    }

    @GetMapping("/api/booking/history")
    public BaseResponse<Page<BookingHistoryResponse>> history(@RequestParam(required = false) List<PaymentStatus> status, Pageable pageable) {
        return new BaseResponse<>(200, "success", bookingService.history(status, pageable));
    }
}
