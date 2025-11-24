package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.ew.UserEWUsageResponse;
import com.capstone.capstone.entity.EWPrice;
import com.capstone.capstone.entity.EWUsage;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.EWPriceRepository;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class EWUsageService {
    private final EWUsageRepository ewUsageRepository;
    private final ModelMapper modelMapper;
    private final SemesterService semesterService;
    private final EWPriceRepository eWPriceRepository;

    @Transactional
    public PagedModel<UserEWUsageResponse> getUserUsages(Map<String, Object> filter, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        var query = new SpecQuery<EWUsage>();
        query.equal("user", user);
        return new PagedModel<>(ewUsageRepository.findAll(query.and(), pageable).map(it -> modelMapper.map(it, UserEWUsageResponse.class)));
    }

    public Object getUserUsagesCount() {
        User user = SecurityUtils.getCurrentUser();
        Semester semester = semesterService.getCurrent().orElseThrow();
        var query = new SpecQuery<EWUsage>();
        query.equal("user", user);
        query.equal("semester", semester);
        List<EWUsage> usages = ewUsageRepository.findAll(query.and());
        EWPrice price = eWPriceRepository.getCurrent().orElse(null);
        int totalElectric = usages.stream().mapToInt(EWUsage::getElectric).sum();
        int totalWater = usages.stream().mapToInt(EWUsage::getWater).sum();
        Map<String, Object> response = new HashMap<>();
        response.put("electric", totalElectric);
        response.put("water", totalWater);
        if (price != null) {
            int electricOverflow = totalElectric > price.getMaxElectricIndex() ? totalElectric - price.getMaxElectricIndex() : 0;
            int waterOverflow = totalWater > price.getMaxWaterIndex() ? totalWater - price.getMaxWaterIndex() : 0;
            long electricPrice = electricOverflow * price.getElectricPrice();
            long waterPrice = waterOverflow * price.getWaterPrice();
            response.put("electricPrice", electricPrice);
            response.put("waterPrice", waterPrice);
            response.put("electricOverflow", electricOverflow);
            response.put("waterOverflow", waterOverflow);
        }
        return response;
    }
}
