package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.ew.CreateEWRoomRequest;
import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.entity.EWRoom;
import com.capstone.capstone.entity.EWUsage;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.EWRoomRepository;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EWRoomService {
    private final EWRoomRepository ewRoomRepository;
    private final EWUsageRepository ewUsageRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;

    public EWRoom create(int electric, int water) {
        return ewRoomRepository.save(EWRoom.builder()
                .electric(electric)
                .water(water)
                .createTime(LocalDateTime.now())
                .build());
    }

    public PagedModel<EWRoomResponse> getResponseByRoom(UUID roomId, Pageable pageable) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createTime"));
        return new PagedModel<>(ewRoomRepository.findAll((r, q, c) -> {
            return c.equal(r.get("room"), room);
        }, pageRequest).map(ewRoom -> modelMapper.map(ewRoom, EWRoomResponse.class)));
    }

    public EWRoomResponse create(UUID roomId, @Valid CreateEWRoomRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        EWRoom ewRoom = new EWRoom();
        ewRoom.setRoom(room);
        ewRoom.setElectric(request.getElectric());
        ewRoom.setWater(request.getWater());
        var createTime = LocalDateTime.now();
        ewRoom.setCreateTime(createTime);
        ewRoom = ewRoomRepository.save(ewRoom);

        var latest = ewRoomRepository.findLatest(room).orElse(null);

        List<User> users = roomRepository.findUsers(room);
        for (User user : users) {
            EWUsage ewUsage = new EWUsage();
            ewUsage.setUser(user);
            if (latest == null) {
                ewUsage.setElectric(request.getElectric());
                ewUsage.setWater(request.getWater());
                ewUsage.setStartTime(createTime);
                ewUsage.setEndTime(createTime);
            } else {
                int electric = request.getElectric() - latest.getElectric();
                int water = request.getWater() - latest.getWater();
                if (electric == 0 || water == 0) break;
                if (electric < 0) throw new AppException("ELECTRIC_USED_NEGATIVE");
                if (water < 0) throw new AppException("WATER_USED_NEGATIVE");
                ewUsage.setElectric(electric);
                ewUsage.setWater(water);
                ewUsage.setStartTime(latest.getCreateTime());
                ewUsage.setEndTime(createTime);
            }
            ewUsage.setPaid(false);
            ewUsageRepository.save(ewUsage);
        }

        return modelMapper.map(ewRoom, EWRoomResponse.class);
    }
}
