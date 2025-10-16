package com.capstone.capstone.dto.response.room;

import lombok.Data;

import java.util.UUID;

@Data
public class RoommateResponse {
    private UUID id;
    private String username;
    private String email;
    private double matching;
}
