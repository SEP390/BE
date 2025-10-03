package com.capstone.capstone.dto.response.surveySellect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateQuestionSelectedResponse {
    private List<UUID> ids;
}
