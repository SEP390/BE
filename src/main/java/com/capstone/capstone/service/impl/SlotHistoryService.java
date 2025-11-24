package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;
    private final ModelMapper modelMapper;

    public PagedModel<SlotHistoryResponse> getAllByCurrentUser(Map<String, Object> filter, Pageable pageable) {
        var user = SecurityUtils.getCurrentUser();
        SpecQuery<SlotHistory> query = new SpecQuery<>();
        query.equal("user", user);
        return new PagedModel<>(slotHistoryRepository.findAll(query.and(), pageable).map(sh -> modelMapper.map(sh, SlotHistoryResponse.class)));
    }

    public boolean existsByUser(User user) {
        return slotHistoryRepository.existsByUser(user);
    }

    public PagedModel<SlotHistoryResponse> getAll(Map<String, Object> filter, Pageable pageable) {
        SpecQuery<SlotHistory> query = new SpecQuery<>();
        query.equal(filter, "userId");
        query.equal(filter, "roomId");
        return new PagedModel<>(slotHistoryRepository.findAll(query.and(), pageable).map(sh -> modelMapper.map(sh, SlotHistoryResponse.class)));
    }
}
