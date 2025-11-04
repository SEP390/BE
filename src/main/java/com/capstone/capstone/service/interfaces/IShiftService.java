package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.shift.CreateShiftRequest;
import com.capstone.capstone.dto.request.shift.UpdateShiftRequest;
import com.capstone.capstone.dto.response.shift.CreateShiftResponse;
import com.capstone.capstone.dto.response.shift.GetAllShiftResponse;
import com.capstone.capstone.dto.response.shift.UpdateShiftResponse;

import java.util.List;
import java.util.UUID;

public interface IShiftService {
    CreateShiftResponse createShift(CreateShiftRequest request);
    List<GetAllShiftResponse> getAllShifts();
    UpdateShiftResponse updateShift(UUID shiftId, UpdateShiftRequest request);

}
