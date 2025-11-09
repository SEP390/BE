package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;

import java.util.List;

public interface IScheduleService {
    List<CreateScheduleResponse> createSchedule(CreateScheduleRequest request);
}
