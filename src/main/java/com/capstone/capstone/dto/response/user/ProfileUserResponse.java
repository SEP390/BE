package com.capstone.capstone.dto.response.user;

import com.capstone.capstone.dto.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileUserResponse {
    private UUID id;
    private String username;
    private String email;
    private Date dob;
    private GenderEnum gender;
}
