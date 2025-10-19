package com.capstone.capstone.dto.response.request;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
import com.capstone.capstone.entity.Semester;
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
public class GetAllRequestResponse {
    private UUID requestId;
    private LocalDateTime createTime;
    private RequestStatusEnum responseStatus;
    private RequestTypeEnum requestType;
    private Semester semester;
}
