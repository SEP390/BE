package com.capstone.capstone.dto.response.electricwater;

import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

import java.util.UUID;

@Data
public class ElectricWaterIndexResponse {
    private UUID id;
    private Integer electricIndex;
    private Integer waterIndex;
    private SemesterResponse semester;
}
