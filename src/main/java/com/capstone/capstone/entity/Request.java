package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Request extends BaseEntity {
    private String content;
    private String responseMessage;
    private LocalDateTime executeTime;
    private LocalDateTime createTime;
    private String roomNumber;
    @Enumerated(EnumType.STRING)
    private RequestTypeEnum requestType;
    @Enumerated(EnumType.STRING)
    private RequestStatusEnum requestStatus;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    @JsonIgnore
    private Semester semester;
}
