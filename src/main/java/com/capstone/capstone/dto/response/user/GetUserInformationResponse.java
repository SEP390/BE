package com.capstone.capstone.dto.response.user;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUserInformationResponse {
    UUID  id;
    private String username;
    private String email;
    private LocalDate dob;
    private String StudentId;
    private GenderEnum gender;
    private String slotName;
    private String image;
    private RoleEnum role;
}
