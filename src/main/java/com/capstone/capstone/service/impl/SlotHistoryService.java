package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.slotHistory.SlotHistoryResponse;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;
    private final ModelMapper modelMapper;

    public PagedModel<SlotHistoryResponse> getByCurrentUser(Pageable pageable) {
        var user = SecurityUtils.getCurrentUser();
        return new PagedModel<>(slotHistoryRepository.findAll((r, q, c) -> {
            return c.equal(r.get("user"), user);
        }, pageable).map(sh -> modelMapper.map(sh, SlotHistoryResponse.class)));
    }
}
