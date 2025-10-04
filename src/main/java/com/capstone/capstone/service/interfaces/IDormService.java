package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;

import java.util.List;

public interface IDormService {
    /**
     * Get bookable dorm list
     * @param totalSlot type of room
     * @param gender gender of resident in room must be
     * @return dorm list
     */
    List<BookableDormResponse> getBookableDorm(int totalSlot, GenderEnum gender);
}
