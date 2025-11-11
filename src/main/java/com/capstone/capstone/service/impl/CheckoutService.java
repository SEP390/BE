package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CheckoutService {
    private final UserRepository userRepository;
    private final RoomService roomService;
    private final SemesterService semesterService;

    public void checkout(UUID userId) {
        Semester semester = semesterService.getCurrent().orElseThrow();
        // chỉ được checkout từ đầu kỳ đến 1 tuần trước kết thúc kỳ
        if (ChronoUnit.DAYS.between(LocalDate.now(), semester.getEndDate()) < 7) {
            throw new AppException("CHECKOUT_OVERDUE");
        }
        User user = userRepository.findById(userId).orElseThrow();
        Slot slot = roomService.getSlotByUser(user).orElseThrow();
        roomService.checkout(slot);
    }
}
