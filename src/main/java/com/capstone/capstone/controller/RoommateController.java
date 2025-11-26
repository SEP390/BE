package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.user.CoreUserResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SurveySelectRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoommateController {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SurveySelectRepository surveySelectRepository;

    @GetMapping("/api/roommate/{id}")
    @Transactional
    public BaseResponse<?> getRoommateDetail(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser();
        User roommate = userRepository.findById(id).orElseThrow();
        if (user.getSlot().getId().equals(roommate.getSlot().getId())) throw new AppException("UNAUTHORIZED");
        Map<String, Object> res = new HashMap<>();
        res.put("user", modelMapper.map(roommate, CoreUserResponse.class));
        var currentUserSelected = surveySelectRepository.findAllByUser(user);
        var roommateSelected = surveySelectRepository.findAllByUser(roommate);
        var common = currentUserSelected.stream().filter(s -> {
            return roommateSelected.stream().anyMatch(s2 -> s2.getSurveyOption().getId().equals(s.getSurveyOption().getId()));
        }).toList();
        res.put("similar", common.stream().map(s -> new String[]{s.getSurveyOption().getSurveyQuestion().getQuestionContent(), s.getSurveyOption().getOptionContent()}));
        return new BaseResponse<>(res);
    }
}
