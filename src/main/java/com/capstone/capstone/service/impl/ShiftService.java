package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.Shift.CreateShiftRequest;
import com.capstone.capstone.dto.response.Shift.CreateShiftResponse;
import com.capstone.capstone.dto.response.Shift.GetAllShiftResponse;
import com.capstone.capstone.entity.Shift;
import com.capstone.capstone.repository.ShiftRepository;
import com.capstone.capstone.service.interfaces.IShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<GetAllShiftResponse> getAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();
        List<GetAllShiftResponse> shiftResponseList = new ArrayList<>();
        for (Shift shift : shifts) {
            GetAllShiftResponse getAllShiftResponse = new GetAllShiftResponse();
            getAllShiftResponse.setId(shift.getId());
            getAllShiftResponse.setName(shift.getName());
            getAllShiftResponse.setStartTime(shift.getStartTime());
            getAllShiftResponse.setEndTime(shift.getEndTime());
            shiftResponseList.add(getAllShiftResponse);
        }
        return shiftResponseList;
    }
}
