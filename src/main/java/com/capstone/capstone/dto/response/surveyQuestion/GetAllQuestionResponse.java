package com.capstone.capstone.dto.response.surveyQuestion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetAllQuestionResponse {
    private UUID id;
    private String questionContent;
}
