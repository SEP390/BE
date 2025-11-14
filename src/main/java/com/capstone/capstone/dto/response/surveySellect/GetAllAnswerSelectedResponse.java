package com.capstone.capstone.dto.response.surveySellect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetAllAnswerSelectedResponse {
    private UUID questionId;
    private String questionContent;
    private UUID optionSelectedId;
    private String optionSelected;
}
