package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.ew.CreateEWPriceRequest;
import com.capstone.capstone.dto.response.ew.EWPriceResponse;
import com.capstone.capstone.entity.EWPrice;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.EWPriceRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class EWPriceService {
    private final EWPriceRepository ewPriceRepository;
    private final ModelMapper modelMapper;

    public EWPriceResponse getCurrentResponse() {
        var current = ewPriceRepository.getCurrent().orElseThrow(() -> new AppException("PRICE_NOT_FOUND"));
        return modelMapper.map(current, EWPriceResponse.class);
    }

    public EWPriceResponse create(CreateEWPriceRequest request) {
        EWPrice price = modelMapper.map(request, EWPrice.class);
        price.setCreateTime(LocalDateTime.now());
        return modelMapper.map(ewPriceRepository.save(price), EWPriceResponse.class);
    }
}
