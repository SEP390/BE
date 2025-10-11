package com.capstone.capstone.dto.request.request;

import com.capstone.capstone.dto.enums.RequestTypeEnum;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRequestRequest {
    private RequestTypeEnum requestType;
    private String content;
}
