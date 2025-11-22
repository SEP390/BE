package com.capstone.capstone.dto.response.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAllAnonymousRequestResponse {
    private UUID requestId;
    private LocalDateTime createTime;
    private String semesterName;
    private String content;
}
