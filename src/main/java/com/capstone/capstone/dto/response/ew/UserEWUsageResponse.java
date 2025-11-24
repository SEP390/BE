package com.capstone.capstone.dto.response.ew;

import com.capstone.capstone.entity.EWRoom;
import com.capstone.capstone.entity.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserEWUsageResponse {
    private Integer electric;
    private Integer water;
    private Boolean paid;
    private LocalDate startDate;
    private LocalDate endDate;
}
