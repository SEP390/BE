package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.ew.CreateEWRoomRequest;
import com.capstone.capstone.dto.request.ew.UpdateEWRoomRequest;
import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.EWRoomRepository;
import com.capstone.capstone.repository.EWUsageRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.util.SpecQuery;
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
import java.util.Map;
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

    @Transactional
    public EWRoomResponse create(CreateEWRoomRequest request) {
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
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

        if (recentInCurrentSemester != null && recentInCurrentSemester.getCreateDate().equals(LocalDate.now()))
            throw new AppException("ALREADY_CREATE");

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
        ewRoom.setElectricUsed(electricUsed);
        ewRoom.setWaterUsed(waterUsed);
        ewRoom.setSemester(semester);
        ewRoom = ewRoomRepository.save(ewRoom);
        for (User user : users) {
            EWUsage ewUsage = new EWUsage();
            ewUsage.setEwRoom(ewRoom);
            ewUsage.setUser(user);
            ewUsage.setSemester(semester);
            ewUsage.setElectric(electricUsed);
            ewUsage.setWater(waterUsed);
            ewUsage.setStartDate(startDate);
            ewUsage.setEndDate(today);
            ewUsage.setPaid(false);
            ewUsageRepository.save(ewUsage);
        }
        return modelMapper.map(ewRoom, EWRoomResponse.class);
    }

    public PagedModel<EWRoomResponse> getAll(Map<String, Object> filter, Pageable pageable) {
        SpecQuery<EWRoom> query = new SpecQuery<>();
        query.equal(filter, r -> r.get("room").get("id"), "roomId");
        query.equal(filter, r -> r.get("semester").get("id"), "semesterId");
        query.betweenDate(filter, "createDate", "startDate", "endDate");
        return new PagedModel<>(ewRoomRepository.findAll(query.and() , pageable).map(ewRoom -> modelMapper.map(ewRoom, EWRoomResponse.class)));
    }

    public EWRoomResponse getLatest(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return modelMapper.map(ewRoomRepository.findRecent(room), EWRoomResponse.class);
    }

    @Transactional
    public EWRoomResponse update(UpdateEWRoomRequest request) {
        EWRoom ewRoom = ewRoomRepository.findById(request.getId()).orElseThrow(() -> new AppException("EW_ROOM_NOT_FOUND"));
        if (!ewRoom.getCreateDate().equals(LocalDate.now())) throw new AppException("OVERDUE");
        List<EWUsage> usages = ewRoom.getUsages();
        if (usages.stream().anyMatch(u -> u.getPaid().equals(true))) {
            throw new AppException("ALREADY_PAID");
        }
        var newElectric = request.getElectric();
        var newWater = request.getWater();
        var recentElectric = ewRoom.getElectric() - ewRoom.getElectricUsed();
        var recentWater = ewRoom.getWater() - ewRoom.getWaterUsed();
        var electricUsed = newElectric - recentElectric;
        var waterUsed = newWater - recentWater;
        if (electricUsed < 0) throw new AppException("ELECTRIC_USED_NEGATIVE");
        if (waterUsed < 0) throw new AppException("WATER_USED_NEGATIVE");
        ewRoom.setElectric(newElectric);
        ewRoom.setWater(newWater);
        ewRoom.setElectricUsed(electricUsed);
        ewRoom.setWaterUsed(waterUsed);
        ewRoom = ewRoomRepository.save(ewRoom);
        usages.forEach(u -> {
            u.setElectric(electricUsed);
            u.setWater(waterUsed);
        });
        usages = ewUsageRepository.saveAll(usages);
        return modelMapper.map(ewRoom, EWRoomResponse.class);
    }
}
