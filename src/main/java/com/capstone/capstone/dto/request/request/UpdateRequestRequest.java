package com.capstone.capstone.dto.request.request;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateRequestRequest {
    private RequestStatusEnum requestStatus;
    private String responseMessage;
    private LocalDateTime executeTime;
}
