package com.capstone.capstone.dto.response.user;

import com.capstone.capstone.dto.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUserInformationResponse {
    private String username;
    private String email;
    private Date dob;
    private String StudentId;
    private GenderEnum gender;
    private String slotName;
}
