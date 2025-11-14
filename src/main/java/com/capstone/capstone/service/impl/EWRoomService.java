package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.entity.EWRoom;
import com.capstone.capstone.repository.EWRoomRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class EWRoomService {
    private final EWRoomRepository ewRoomRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public EWRoomResponse getLatestResponse() {
        var res = getLatest().orElseThrow();
        return modelMapper.map(res, EWRoomResponse.class);
    }

    public Optional<EWRoom> getLatest() {
        var page = ewRoomRepository.findAll(PageRequest.of(0, 1, Sort.Direction.DESC, "createTime"));
        if (page.getSize() == 0) return Optional.empty();
        return Optional.of(page.getContent().getFirst());
    }

    public EWRoom create(int electric, int water) {
        return ewRoomRepository.save(EWRoom.builder()
                .electric(electric)
                .water(water)
                .build());
    }
}
