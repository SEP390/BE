package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.service.interfaces.IDormService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DormService implements IDormService {
    private final DormRepository dormRepository;

    @Override
    public List<BookableDormResponse> getBookableDorm(int totalSlot, GenderEnum gender) {
        return dormRepository.getBookableDorm(totalSlot, gender);
    }
}
