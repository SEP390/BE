package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.Shift.CreateShiftRequest;
import com.capstone.capstone.dto.response.Shift.CreateShiftResponse;
import com.capstone.capstone.entity.Shift;
import com.capstone.capstone.repository.ShiftRepository;
import com.capstone.capstone.service.interfaces.IShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftService implements IShiftService {
    private final ShiftRepository shiftRepository;

    @Override
    public CreateShiftResponse createShift(CreateShiftRequest request) {
        Shift shift = new Shift();
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift = shiftRepository.save(shift);
        CreateShiftResponse createShiftResponse = new CreateShiftResponse();
        createShiftResponse.setId(shift.getId());
        createShiftResponse.setName(shift.getName());
        createShiftResponse.setStartTime(shift.getStartTime());
        createShiftResponse.setEndTime(shift.getEndTime());
        return createShiftResponse;
    }
}
