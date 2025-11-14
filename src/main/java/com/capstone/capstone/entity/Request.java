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
    private String content; //1
    private String responseByEmployeeMessage; //2 to manager
    private String responseByManagerMessage; //3 to residen
    private LocalDateTime executeTime; //4
    private LocalDateTime createTime; //5
    private String roomNumber; //6
    @Enumerated(EnumType.STRING)
    private RequestTypeEnum requestType; //7
    @Enumerated(EnumType.STRING)
    private RequestStatusEnum requestStatus; //8
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user; //9 nguowif tao don

    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    @JsonIgnore
    private Semester semester;//10
}
