package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.timeConfig.CreateTimeConfigRequest;
import com.capstone.capstone.dto.response.timeConfig.TimeConfigResponse;
import com.capstone.capstone.entity.TimeConfig;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.TimeConfigRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TimeConfigService {
    private final TimeConfigRepository timeConfigRepository;
    private final ModelMapper modelMapper;

    public Optional<TimeConfig> getCurrent() {
        return timeConfigRepository.findCurrent();
    }

    public TimeConfigResponse getCurrentResponse() {
        var current = getCurrent().orElseThrow(() -> new AppException("TIME_CONFIG_NOT_FOUND"));
        return modelMapper.map(current, TimeConfigResponse.class);
    }

    public TimeConfigResponse create(CreateTimeConfigRequest request) {
        TimeConfig timeConfig = modelMapper.map(request, TimeConfig.class);
        timeConfig.setCreateTime(LocalDateTime.now());
        timeConfig = timeConfigRepository.save(timeConfig);
        return modelMapper.map(timeConfig, TimeConfigResponse.class);
    }
}
