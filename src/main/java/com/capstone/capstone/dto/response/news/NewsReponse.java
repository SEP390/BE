package com.capstone.capstone.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewsReponse {
    private UUID usersID;
    private String title;
    private String content;
    private Date date;
    private Time time;
    private String name;
}
