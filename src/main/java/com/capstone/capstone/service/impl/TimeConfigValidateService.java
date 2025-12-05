package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.TimeConfig;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class TimeConfigValidateService {
    private final TimeConfigService timeConfigService;
    private final SlotHistoryService slotHistoryService;

    public void validate() {
        User user = SecurityUtils.getCurrentUser();
        TimeConfig timeConfig = timeConfigService.getCurrent().orElseThrow(() -> new AppException("TIME_CONFIG_NOT_FOUND"));
        var today = LocalDate.now();
        // đã từng dặt phòng
        if (slotHistoryService.existsByUser(user)) {
            if (today.isAfter(timeConfig.getEndExtendDate()) || today.isBefore(timeConfig.getStartExtendDate()))
                throw new AppException("BOOKING_DATE_NOT_START");
        } else {
            if (today.isAfter(timeConfig.getEndBookingDate()) || today.isBefore(timeConfig.getStartBookingDate()))
                throw new AppException("BOOKING_DATE_NOT_START");
        }
    }
}
