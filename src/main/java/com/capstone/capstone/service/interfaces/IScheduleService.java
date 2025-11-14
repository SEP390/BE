package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.request.schedule.UpdateScheduleRequest;
import com.capstone.capstone.dto.response.PageResponse;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;
import com.capstone.capstone.dto.response.schedule.GetScheduleResponse;
import com.capstone.capstone.dto.response.schedule.UpdateScheduleResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IScheduleService {
    List<CreateScheduleResponse> createSchedule(CreateScheduleRequest request);
    PageResponse<GetScheduleResponse> getAllSchedules(Pageable pageable);
    UpdateScheduleResponse updateSchedule(UpdateScheduleRequest request, UUID scheduleId);
}
