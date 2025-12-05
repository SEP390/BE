package com.capstone.capstone.dto.response.ew;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import com.capstone.capstone.repository.SemesterRepository;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EWRoomResponse {
    private UUID id;
    private RoomResponseJoinDorm room;
    private SemesterResponse semester;
    private Integer electric;
    private Integer water;
    private Integer electricUsed;
    private Integer waterUsed;
    private LocalDate createDate;
}
