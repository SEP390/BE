package com.capstone.capstone.dto.response.surveyQuestion;

import com.capstone.capstone.dto.response.surveyOption.GetOptionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetQuestionByIdResponse {
    private UUID id;
    private String questionContent;
    private List<GetOptionResponse> options;
}
