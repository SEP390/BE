package com.capstone.capstone.dto.response.surveyOption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOptionResponse {
    private UUID id;
    private String optionContent;
}
