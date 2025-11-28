package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.ew.CreateEWPriceRequest;
import com.capstone.capstone.entity.EWPrice;
import com.capstone.capstone.repository.EWPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EWPriceServiceTest {
    @InjectMocks
    EWPriceService ewPriceService;
    @Mock
    EWPriceRepository ewPriceRepository;
    @Spy
    ModelMapper modelMapper;

    EWPrice ewPrice;

    @Test
    void test_getCurrentResponse_Success() {
        ewPrice = new EWPrice();
        when(ewPriceRepository.getCurrent()).thenReturn(Optional.of(ewPrice));
        assertThat(ewPriceService.getCurrentResponse()).isNotNull();
    }

    @Test
    void test_create_Success() {
        ewPrice = new EWPrice();
        var request = new CreateEWPriceRequest();
        when(ewPriceRepository.save(any(EWPrice.class))).thenReturn(ewPrice);
        assertThat(ewPriceService.create(request)).isNotNull();
    }
}