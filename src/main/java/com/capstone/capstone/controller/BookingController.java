package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.service.impl.BookingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/api/booking/create")
    public BaseResponse<CreateBookingResponse> create(@RequestBody @Valid CreateBookingRequest request) {
        return new BaseResponse<>(bookingService.create(request));
    }

    @GetMapping("/api/booking/current")
    public BaseResponse<SlotResponseJoinRoomAndDormAndPricing> current() {
        return new BaseResponse<>(bookingService.current());
    }

    @GetMapping("/api/booking/history")
    public BaseResponse<PagedModel<BookingHistoryResponse>> history(@RequestParam(required = false) List<PaymentStatus> status, Pageable pageable) {
        return new BaseResponse<>(bookingService.history(status, pageable));
    }
}
