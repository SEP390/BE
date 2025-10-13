package com.capstone.capstone.dto.request.news;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewsRequest {
    private UUID usersId;
    private String title;
    private String content;
    private LocalDate date;
    private LocalTime time;
}
