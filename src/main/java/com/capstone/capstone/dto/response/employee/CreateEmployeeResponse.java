package com.capstone.capstone.dto.response.employee;

import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateEmployeeResponse {
    private UUID employeeId;
    private String email;
    private String username;
    private RoleEnum role;
}
