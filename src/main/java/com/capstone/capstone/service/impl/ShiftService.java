package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.shift.CreateShiftRequest;
import com.capstone.capstone.dto.request.shift.UpdateShiftRequest;
import com.capstone.capstone.dto.response.shift.CreateShiftResponse;
import com.capstone.capstone.dto.response.shift.GetAllShiftResponse;
import com.capstone.capstone.dto.response.shift.UpdateShiftResponse;
import com.capstone.capstone.entity.Schedule;
import com.capstone.capstone.entity.Shift;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.ScheduleRepository;
import com.capstone.capstone.repository.ShiftRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IShiftService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService implements IShiftService {
    private final ShiftRepository shiftRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public CreateShiftResponse createShift(CreateShiftRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
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

    @Override
    public UpdateShiftResponse updateShift(UUID shiftId, UpdateShiftRequest request) {
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(()-> new NotFoundException("Shift not found"));
        Schedule schedule = scheduleRepository.findByShift(shift);
        if(schedule != null) throw new RuntimeException("Schedule exited so cannot update shift");
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift = shiftRepository.save(shift);
        UpdateShiftResponse updateShiftResponse = new UpdateShiftResponse();
        updateShiftResponse.setName(shift.getName());
        updateShiftResponse.setStartTime(shift.getStartTime());
        updateShiftResponse.setEndTime(shift.getEndTime());
        return updateShiftResponse;
    }
}
