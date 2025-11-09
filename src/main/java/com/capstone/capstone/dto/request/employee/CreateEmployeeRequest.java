package com.capstone.capstone.dto.request.employee;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateEmployeeRequest {
    private String username;
    private String fullName;
    private String password;
    private String userCode;
    private String email;
    private LocalDate dob;
    private GenderEnum gender;
    private RoleEnum role;
    private String phoneNumber;
    private LocalDate hireDate;
    private LocalDate contractEndDate;
}
