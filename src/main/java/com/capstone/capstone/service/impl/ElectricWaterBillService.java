package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterBillRequest;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterRoomBillResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.ElectricWaterBillRepository;
import com.capstone.capstone.repository.ElectricWaterRoomBillRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ElectricWaterBillService {
    private final RoomRepository roomRepository;
    private final ElectricWaterRoomBillRepository electricWaterRoomBillRepository;
    private final ElectricWaterBillRepository electricWaterBillRepository;
    private final ModelMapper modelMapper;
    private final SemesterService semesterService;
    private final UserRepository userRepository;

    @Transactional
    public ElectricWaterRoomBillResponse create(CreateElectricWaterBillRequest request) {
        Room room = roomRepository.getReferenceById(request.getRoomId());
        Semester semester = semesterService.getCurrent();
        List<User> users = roomRepository.findUsers(room);
        if (users.isEmpty()) {
            throw new AppException("NO_USER_IN_ROOM");
        }
        long price = request.getPrice();
        int kw = request.getKw();
        int m3 = request.getM3();

        ElectricWaterRoomBill roomBill = ElectricWaterRoomBill.builder()
                .room(room)
                .semester(semester)
                .price(price)
                .kw(kw)
                .m3(m3)
                .build();
        roomBill = electricWaterRoomBillRepository.save(roomBill);
        long userPrice = Math.round(Math.ceil((double) price / users.size()));
        for (User user : users) {
            ElectricWaterBill bill = ElectricWaterBill.builder()
                    .user(user)
                    .price(userPrice)
                    .roomBill(roomBill)
                    .semester(semester)
                    .status(PaymentStatus.PENDING)
                    .createDate(LocalDateTime.now())
                    .build();
            electricWaterBillRepository.save(bill);
        }
        return modelMapper.map(roomBill, ElectricWaterRoomBillResponse.class);
    }

    public ElectricWaterRoomBillResponse getByRoomId(UUID id) {
        Room room = new Room();
        room.setId(id);
        Semester semester = semesterService.getCurrent();
        ElectricWaterRoomBill example = new ElectricWaterRoomBill();
        example.setRoom(room);
        example.setSemester(semester);
        ElectricWaterRoomBill roomBill = electricWaterRoomBillRepository.findOne(Example.of(example)).orElse(null);
        if (roomBill == null) return null;
        return modelMapper.map(roomBill, ElectricWaterRoomBillResponse.class);
    }

    public List<ElectricWaterBillResponse> getCurrent() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        ElectricWaterBill example = new ElectricWaterBill();
        example.setUser(user);
        List<ElectricWaterBill> bills = electricWaterBillRepository.findAll(Example.of(example));
        return bills.stream().map(bill -> modelMapper.map(bill, ElectricWaterBillResponse.class)).toList();
    }

    public void onPayment(ElectricWaterBill bill, VNPayStatus status) {
        if (status == VNPayStatus.SUCCESS) {
            bill.setStatus(PaymentStatus.SUCCESS);
        } else {
            bill.setStatus(PaymentStatus.CANCEL);
        }
        electricWaterBillRepository.save(bill);
    }

    public ElectricWaterBill getById(UUID id) {
        return electricWaterBillRepository.findById(id).orElse(null);
    }
}
