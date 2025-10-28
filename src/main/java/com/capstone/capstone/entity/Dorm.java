package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.DormStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dorm extends BaseEntity {
    private String dormName;
    private Integer totalRoom;
    private Integer totalFloor;

    @OneToMany(mappedBy = "dorm")
    private List<Room> rooms;

    @OneToMany(mappedBy = "dorm")
    private List<Employee> employees;

    @Enumerated(EnumType.STRING)
    private DormStatus status;
}
