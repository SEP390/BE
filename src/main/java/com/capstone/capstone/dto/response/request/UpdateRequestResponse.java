package com.capstone.capstone.dto.response.request;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateRequestResponse {
    private UUID requestId;
    private UUID useId;
    private RequestStatusEnum requestStatus;
    private LocalDateTime executeTime;
    private String responseMessage;
}
