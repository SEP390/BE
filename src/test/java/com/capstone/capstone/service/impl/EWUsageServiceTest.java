package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.ew.UserEWUsageResponse;
import com.capstone.capstone.entity.EWPrice;
import com.capstone.capstone.entity.EWUsage;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.EWPriceRepository;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EWUsageServiceTest {
    @Mock
    private EWUsageRepository ewUsageRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private SemesterService semesterService;
    @Mock
    private EWPriceRepository eWPriceRepository;
    @InjectMocks
    private EWUsageService ewUsageService;
    private User mockUser;
    private Semester mockSemester;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockSemester = new Semester();
        mockSemester.setId(UUID.randomUUID());
    }

    private EWUsage createEWUsage(int electric, int water) {
        EWUsage usage = new EWUsage();
        usage.setElectric(electric);
        usage.setWater(water);
        usage.setUser(mockUser);
        usage.setSemester(mockSemester);
        return usage;
    }

    @Test
    void getUserUsages_ShouldReturnPagedModel_AndFilterByCurrentUser() {
        Map<String, Object> filter = Map.of("semesterId", mockSemester.getId().toString(), "startDate", LocalDate.of(2023, 1, 1), "endDate", LocalDate.of(2023, 1, 31));
        Pageable pageable = PageRequest.of(0, 10);
        List<EWUsage> usageList = List.of(createEWUsage(10, 5), createEWUsage(20, 15));
        Page<EWUsage> usagePage = new PageImpl<>(usageList, pageable, 2);
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(ewUsageRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(usagePage);
            UserEWUsageResponse response1 = new UserEWUsageResponse();
            UserEWUsageResponse response2 = new UserEWUsageResponse();
            when(modelMapper.map(eq(usageList.get(0)), eq(UserEWUsageResponse.class))).thenReturn(response1);
            when(modelMapper.map(eq(usageList.get(1)), eq(UserEWUsageResponse.class))).thenReturn(response2);
            PagedModel<UserEWUsageResponse> result = ewUsageService.getUserUsages(filter, pageable);
            verify(ewUsageRepository).findAll(any(Specification.class), eq(pageable));
            verify(modelMapper, times(2)).map(any(EWUsage.class), eq(UserEWUsageResponse.class));
            assertThat(result.getContent()).hasSize(2).containsExactly(response1, response2);
            assertThat(result.getMetadata().totalElements()).isEqualTo(2);
        }
    }

    @Test
    void getUserUsages_ShouldHandleEmptyFilter() {
        Map<String, Object> filter = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);
        Page<EWUsage> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(ewUsageRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
            PagedModel<UserEWUsageResponse> result = ewUsageService.getUserUsages(filter, pageable);
            assertThat(result.getContent()).isEmpty();
            verify(ewUsageRepository).findAll(any(Specification.class), eq(pageable));
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Test
    void getUserUsagesCount_Case1_Success_WithPriceAndOverflow() {
        EWPrice mockPrice = new EWPrice();
        mockPrice.setMaxElectricIndex(20);
        mockPrice.setMaxWaterIndex(10);
        mockPrice.setElectricPrice(100L);
        mockPrice.setWaterPrice(50L);
        List<EWUsage> usages = List.of(createEWUsage(15, 10), createEWUsage(20, 15));
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(ewUsageRepository.findAll(any(Specification.class))).thenReturn(usages);
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.of(mockPrice));
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            Map<String, Object> result = (Map<String, Object>) ewUsageService.getUserUsagesCount();
            assertThat(result.get("electric")).isEqualTo(35);
            assertThat(result.get("water")).isEqualTo(25);
            assertThat(result.get("electricOverflow")).isEqualTo(15);
            assertThat(result.get("waterOverflow")).isEqualTo(15);
            assertThat(result.get("electricPrice")).isEqualTo(1500L);
            assertThat(result.get("waterPrice")).isEqualTo(750L);
            verify(ewUsageRepository).findAll(any(Specification.class));
            verify(eWPriceRepository).getCurrent();
        }
    }

    @Test
    void getUserUsagesCount_Case2_Success_WithPriceAndNoOverflow() {
        EWPrice mockPrice = new EWPrice();
        mockPrice.setMaxElectricIndex(50);
        mockPrice.setMaxWaterIndex(30);
        mockPrice.setElectricPrice(100L);
        mockPrice.setWaterPrice(50L);
        List<EWUsage> usages = List.of(createEWUsage(10, 5), createEWUsage(20, 10));
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(ewUsageRepository.findAll(any(Specification.class))).thenReturn(usages);
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.of(mockPrice));
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            Map<String, Object> result = (Map<String, Object>) ewUsageService.getUserUsagesCount();
            assertThat(result.get("electric")).isEqualTo(30);
            assertThat(result.get("water")).isEqualTo(15);
            assertThat(result.get("electricOverflow")).isEqualTo(0);
            assertThat(result.get("waterOverflow")).isEqualTo(0);
            assertThat(result.get("electricPrice")).isEqualTo(0L);
            assertThat(result.get("waterPrice")).isEqualTo(0L);
        }
    }

    @Test
    void getUserUsagesCount_Case3_Success_WithoutPrice() {
        List<EWUsage> usages = List.of(createEWUsage(15, 10), createEWUsage(20, 15));
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(ewUsageRepository.findAll(any(Specification.class))).thenReturn(usages);
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.empty());
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            Map<String, Object> result = (Map<String, Object>) ewUsageService.getUserUsagesCount();
            assertThat(result.get("electric")).isEqualTo(35);
            assertThat(result.get("water")).isEqualTo(25);
            assertThat(result).doesNotContainKeys("electricPrice", "waterPrice", "electricOverflow", "waterOverflow");
        }
    }

    @Test
    void getUserUsagesCount_Case4_Exception_NoCurrentSemester() {
        when(semesterService.getCurrent()).thenReturn(Optional.empty());
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            assertThatThrownBy(() -> ewUsageService.getUserUsagesCount()).isInstanceOf(NoSuchElementException.class);
        }
        verify(ewUsageRepository, never()).findAll(any(Specification.class));
        verify(eWPriceRepository, never()).getCurrent();
    }

    @Test
    void getUserUsagesCount_Case5_NoUsages() {
        EWPrice mockPrice = new EWPrice();
        mockPrice.setMaxElectricIndex(10);
        mockPrice.setMaxWaterIndex(10);
        mockPrice.setElectricPrice(100L);
        mockPrice.setWaterPrice(50L);
        when(semesterService.getCurrent()).thenReturn(Optional.of(mockSemester));
        when(ewUsageRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(eWPriceRepository.getCurrent()).thenReturn(Optional.of(mockPrice));
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            Map<String, Object> result = (Map<String, Object>) ewUsageService.getUserUsagesCount();
            assertThat(result.get("electric")).isEqualTo(0);
            assertThat(result.get("water")).isEqualTo(0);
            assertThat(result.get("electricOverflow")).isEqualTo(0);
            assertThat(result.get("waterOverflow")).isEqualTo(0);
            assertThat(result.get("electricPrice")).isEqualTo(0L);
            assertThat(result.get("waterPrice")).isEqualTo(0L);
        }
    }
}