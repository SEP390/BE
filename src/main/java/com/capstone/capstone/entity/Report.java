package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.enums.ReportTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Report extends BaseEntity {
    private String content;
    private LocalDateTime createdAt;
    private ReportStatusEnum reportStatus;
    private String responseMessage;
    private String userCode;
    private ReportTypeEnum reportType;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;
}
