package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.ew.UserEWUsageResponse;
import com.capstone.capstone.entity.EWUsage;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@AllArgsConstructor
public class EWUsageService {
    private final EWUsageRepository ewUsageRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PagedModel<UserEWUsageResponse> getUserUsages(Map<String, Object> filter, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        var query = new SpecQuery<EWUsage>();
        query.equal("user", user);
        return new PagedModel<>(ewUsageRepository.findAll(query.and(), pageable).map(it -> modelMapper.map(it, UserEWUsageResponse.class)));
    }
}
