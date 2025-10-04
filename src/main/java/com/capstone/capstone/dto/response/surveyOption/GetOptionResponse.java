package com.capstone.capstone.dto.response.surveyOption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetOptionResponse {
    private UUID id;
    private String optionContent;
}
