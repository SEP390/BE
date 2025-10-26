package com.capstone.capstone.dto.response.employee;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetEmployeeById {
    private UUID userId;
    private UUID employeeId;
    private String userCode;
    private LocalDate dob;
    private String email;
    private GenderEnum gender;
    private RoleEnum role;
    private String phoneNumber;
    private String fullName;
}
