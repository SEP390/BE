package com.capstone.capstone.entity;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import jakarta.persistence.Entity;
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
public class Room extends BaseEntity {
    private String roomNumber;
    private int totalSlot;
    private int floor;
    private StatusRoomEnum status;
    @OneToMany(mappedBy = "room")
    private List<Slot> slots;
}
