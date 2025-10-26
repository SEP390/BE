package com.capstone.capstone.dto.request.employee;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.entity.Dorm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {
    private UUID EmployeeId;
    private UUID dormId;
    private String phoneNumber;
    private LocalDate birthDate;
    private RoleEnum role;
}
