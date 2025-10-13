package com.capstone.capstone.dto.response.news;

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
public class NewsReponse {
    private UUID newsid;
    private UUID userId;
    private String title;
    private String content;
    private LocalDate date;
    private LocalTime time;
    private String name;
}
