package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class RoomUserResponse {
    private UUID id;
    private String username;
    private String email;
    private Date dob;
    private String userCode;
    private GenderEnum gender;
    private RoleEnum role;
}
