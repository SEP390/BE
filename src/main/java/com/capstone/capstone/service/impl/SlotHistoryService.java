package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponseJoinUser;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
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
        query.equal(filter, r -> r.get("room").get("id"), "roomId");
        query.equal(filter, r -> r.get("semester").get("id"), "semesterId");
        return new PagedModel<>(slotHistoryRepository.findAll(query.and(), pageable).map(sh -> modelMapper.map(sh, SlotHistoryResponse.class)));
    }

    public boolean existsByUser(User user) {
        return slotHistoryRepository.existsByUser(user);
    }

    public PagedModel<SlotHistoryResponseJoinUser> getAll(Map<String, Object> filter, Pageable pageable) {
        SpecQuery<SlotHistory> query = new SpecQuery<>();
        query.equal(filter, r -> r.get("user").get("id"), "userId");
        query.equal(filter, r -> r.get("room").get("id"), "roomId");
        query.equal(filter, r -> r.get("semester").get("id"), "semesterId");
        return new PagedModel<>(slotHistoryRepository.findAll(query.and(), pageable).map(sh -> modelMapper.map(sh, SlotHistoryResponseJoinUser.class)));
    }

    public SlotHistoryResponse getCurrent() {
        var user = SecurityUtils.getCurrentUser();
        return modelMapper.map(slotHistoryRepository.findCurrent(user).orElseThrow(() -> new AppException("SLOT_HISTORY_NOT_FOUND")), SlotHistoryResponse.class);
    }
}
