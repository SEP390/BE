package com.capstone.capstone.dto.request.news;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewsRequest {
    private String title;
    private String content;
    private Date date;
    private Time time;
}
