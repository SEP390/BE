package com.capstone.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    private int totalRoom;
    private int totalFloor;

    @OneToMany(mappedBy = "dorm")
    private List<Room> rooms;
}
