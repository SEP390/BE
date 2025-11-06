package com.capstone.capstone.dto.request.dorm;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDormRequest {
    @NotNull(message = "DORM_NAME_NULL")
    private String dormName;
    @NotNull(message = "TOTAL_FLOOR_NULL")
    @Min(value = 1, message = "TOTAL_FLOOR_MIN")
    @Max(value = 255, message = "TOTAL_FLOOR_MAX")
    private Integer totalFloor;
}
