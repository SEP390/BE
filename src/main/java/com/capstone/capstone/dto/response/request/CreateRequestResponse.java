package com.capstone.capstone.dto.response.request;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
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
public class CreateRequestResponse {
    private RequestTypeEnum requestType;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime executeTime;
    private RequestStatusEnum requestStatus;
    private String responseMessage;
    private UUID semesterId;
    private UUID useId;
    private UUID requestId;
}
