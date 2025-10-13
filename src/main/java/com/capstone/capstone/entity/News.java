package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.StatusNewsEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name="News")
public class News  {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID newsid;
    private String title;
    private String content;
    @CreationTimestamp
    private LocalDate date;
    @CreationTimestamp

    private LocalTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    @Enumerated(EnumType.STRING)
    private StatusNewsEnum  status;
}
