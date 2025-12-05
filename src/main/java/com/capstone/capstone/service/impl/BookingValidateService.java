package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SurveySelectRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookingValidateService {
    private final TimeConfigValidateService timeConfigValidateService;
    private final SurveySelectRepository surveySelectRepository;

    public void validate() {
        User user = SecurityUtils.getCurrentUser();
        // validate thời gian đặt phòng
        timeConfigValidateService.validate();
        // không được đặt phòng nếu chưa làm survey
        if (!surveySelectRepository.exists((r, q, c) -> {
            return c.equal(r.get("user"), user);
        })) throw new AppException("SURVEY_NOT_FOUND");
    }
}
