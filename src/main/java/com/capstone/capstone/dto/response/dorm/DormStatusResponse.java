package com.capstone.capstone.dto.response.dorm;

import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DormStatusResponse extends DormResponse {
    private Boolean status;
}
