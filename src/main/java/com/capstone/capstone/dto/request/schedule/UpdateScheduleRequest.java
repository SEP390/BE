package com.capstone.capstone.dto.request.schedule;

import com.capstone.capstone.entity.Shift;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateScheduleRequest {
    private UUID shiftId;
    private String note;
    private UUID dormID;
}
