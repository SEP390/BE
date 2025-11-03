package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.Shift.CreateShiftRequest;
import com.capstone.capstone.dto.response.Shift.CreateShiftResponse;

public interface IShiftService {
    CreateShiftResponse createShift(CreateShiftRequest request);
}
