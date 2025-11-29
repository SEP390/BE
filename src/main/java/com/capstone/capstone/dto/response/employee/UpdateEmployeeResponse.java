package com.capstone.capstone.dto.response.employee;

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
public class UpdateEmployeeResponse {
    private UUID employeeId;
    private String fullName;
    private LocalDate birthDate;
    private LocalDate contractEndDate;
}
