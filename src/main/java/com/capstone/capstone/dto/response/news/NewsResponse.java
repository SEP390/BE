package com.capstone.capstone.dto.response.news;

import com.capstone.capstone.dto.enums.StatusNewsEnum;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NewsResponse {
    private UUID newsid;
    private String title;
    private String content;
    private LocalDate date;
    private LocalTime time;
    private String userNames;
    private StatusNewsEnum status;
}
