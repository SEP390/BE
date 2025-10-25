package com.capstone.capstone.dto.response.employee;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAllEmployeeResponse {
    private UUID employeeId;
    private String username;
    private RoleEnum role;
    private String phone;
    private String dormName;
    private String email;
}
