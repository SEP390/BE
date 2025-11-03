package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.holiday.CreateHolidayRequest;
import com.capstone.capstone.dto.request.holiday.UpdateHolidayRequest;
import com.capstone.capstone.dto.response.holiday.CreateHolidayResponse;
import com.capstone.capstone.dto.response.holiday.GetAllHolidayResponse;
import com.capstone.capstone.dto.response.holiday.UpdateHolidayResponse;

import java.util.List;
import java.util.UUID;

public interface IHolidayService {
    CreateHolidayResponse createHoliday(CreateHolidayRequest request);
    UpdateHolidayResponse updateHoliday(UUID holidayId, UpdateHolidayRequest request);
    List<GetAllHolidayResponse> getAllHoliday();
}
