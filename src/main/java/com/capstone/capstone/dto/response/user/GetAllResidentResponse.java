package com.capstone.capstone.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllResidentResponse {
    private UUID residentId;
    private String userName;
    private String email;
    private String fullName;
    private String phoneNumber;
}
