package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.response.PageResponse;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;
import com.capstone.capstone.dto.response.schedule.GetScheduleResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IScheduleService {
    List<CreateScheduleResponse> createSchedule(CreateScheduleRequest request);
    PageResponse<GetScheduleResponse> getAllSchedules(Pageable pageable);
}
