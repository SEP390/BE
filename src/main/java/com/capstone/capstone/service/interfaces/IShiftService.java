package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.Shift.CreateShiftRequest;
import com.capstone.capstone.dto.response.Shift.CreateShiftResponse;
import com.capstone.capstone.dto.response.Shift.GetAllShiftResponse;

import java.time.LocalTime;
import java.util.List;

public interface IShiftService {
    CreateShiftResponse createShift(CreateShiftRequest request);
    List<GetAllShiftResponse> getAllShifts();
}
