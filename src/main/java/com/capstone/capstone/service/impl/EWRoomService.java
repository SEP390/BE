package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.ew.CreateEWRoomRequest;
import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.EWRoomRepository;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SemesterRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EWRoomService {
    private final EWRoomRepository ewRoomRepository;
    private final EWUsageRepository ewUsageRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final SemesterService semesterService;
    private final SemesterRepository semesterRepository;

    public PagedModel<EWRoomResponse> getResponseByRoom(UUID roomId, Pageable pageable) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createTime"));
        return new PagedModel<>(ewRoomRepository.findAll((r, q, c) -> {
            return c.equal(r.get("room"), room);
        }, pageRequest).map(ewRoom -> modelMapper.map(ewRoom, EWRoomResponse.class)));
    }

    @Transactional
    public EWRoomResponse create(UUID roomId, CreateEWRoomRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        EWRoom ewRoom = new EWRoom();
        ewRoom.setRoom(room);
        ewRoom.setElectric(request.getElectric());
        ewRoom.setWater(request.getWater());
        LocalDate today = LocalDate.now();
        ewRoom.setCreateDate(today);

        Semester semester = semesterService.getCurrent().orElseThrow(() -> new AppException("CURRENT_SEMESTER_NOT_FOUND"));
        Semester previousSemester = semesterRepository.findPrevious().orElseThrow(() -> new AppException("PREVIOUS_SEMESTER_NOT_FOUND"));
        var recentInCurrentSemester = ewRoomRepository.findRecent(room, semester).orElse(null);
        var recentInPreviousSemester = ewRoomRepository.findRecent(room, previousSemester).orElse(null);

        List<User> users = roomRepository.findUsers(room);

        int electricUsed = 0;
        int waterUsed = 0;
        LocalDate startDate;
        // số hiện tại - số gần nhất trong kỳ (ví dụ tháng trước)
        if (recentInCurrentSemester != null) {
            electricUsed = request.getElectric() - recentInCurrentSemester.getElectric();
            waterUsed = request.getWater() - recentInCurrentSemester.getWater();
            // ngày nhập gần nhất
            startDate = recentInCurrentSemester.getCreateDate();
        } else {
            // tháng đầu tiên của kỳ -> trừ đi số gần nhất ở kỳ trước
            if (recentInPreviousSemester != null) {
                electricUsed = request.getElectric() - recentInPreviousSemester.getElectric();
                waterUsed = request.getWater() - recentInPreviousSemester.getWater();
                // ngày đầu tiên của kỳ
            } else {
                electricUsed = request.getElectric();
                waterUsed = request.getWater();
                // ngày đầu tiên của kỳ
            }
            startDate = semester.getStartDate();
        }
        if (electricUsed < 0) throw new AppException("ELECTRIC_USED_NEGATIVE");
        if (waterUsed < 0) throw new AppException("WATER_USED_NEGATIVE");
        ewRoom = ewRoomRepository.save(ewRoom);
        for (User user : users) {
            EWUsage ewUsage = new EWUsage();
            ewUsage.setUser(user);
            ewUsage.setElectric(electricUsed);
            ewUsage.setWater(waterUsed);
            ewUsage.setStartDate(startDate);
            ewUsage.setEndDate(today);
            ewUsage.setPaid(false);
            ewUsageRepository.save(ewUsage);
        }
        return modelMapper.map(ewRoom, EWRoomResponse.class);
    }
}
