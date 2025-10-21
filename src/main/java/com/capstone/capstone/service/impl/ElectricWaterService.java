package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterBillRequest;
import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterIndexRequest;
import com.capstone.capstone.dto.request.electricwater.CreateElectricWaterPricingRequest;
import com.capstone.capstone.dto.request.electricwater.UpdateElectricWaterPricingRequest;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterIndexResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterPricingResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
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
public class ElectricWaterService {
    private final RoomRepository roomRepository;
    private final ElectricWaterIndexRepository electricWaterIndexRepository;
    private final ElectricWaterBillRepository electricWaterBillRepository;
    private final ModelMapper modelMapper;
    private final SemesterService semesterService;
    private final UserRepository userRepository;
    private final ElectricWaterPricingRepository electricWaterPricingRepository;

    @Transactional
    public ElectricWaterIndexResponse createIndexResponse(CreateElectricWaterIndexRequest request) {
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        Semester semester = semesterService.getCurrent();

        // TODO: kiểm tra thời gian đang trong kỳ hay ngoài kỳ

        // Đã tồn tại chỉ số điện nước
        if (getIndexOfRoom(room, semester) != null) {
            throw new AppException("INDEX_EXISTED");
        }

        // Đã validate trong request
        int electricIndex = request.getElectricIndex();
        int waterIndex = request.getWaterIndex();

        // Tạo index
        ElectricWaterIndex index = createIndex(room, semester, electricIndex, waterIndex);

        return modelMapper.map(index, ElectricWaterIndexResponse.class);
    }

    public ElectricWaterIndex createIndex(Room room, Semester semester, int electricIndex, int waterIndex) {
        ElectricWaterIndex index = ElectricWaterIndex.builder()
                .room(room)
                .semester(semester)
                .electricIndex(electricIndex)
                .waterIndex(waterIndex)
                .createDate(LocalDateTime.now())
                .build();
        return electricWaterIndexRepository.save(index);
    }

    @Transactional
    public ElectricWaterBillResponse createBillResponse(CreateElectricWaterBillRequest request) {
        ElectricWaterIndex index = electricWaterIndexRepository.findById(request.getIndexId()).orElseThrow(() -> new AppException("INDEX_NOT_FOUND"));
        ElectricWaterBill bill = createBill(index);
        return modelMapper.map(bill, ElectricWaterBillResponse.class);
    }

    public ElectricWaterBill createBill(ElectricWaterIndex index) {
        // Đếm số user trong phòng
        List<User> users = index.getRoom().getSlots().stream().map(Slot::getUser).filter(Objects::nonNull).toList();

        // Không có user trong phòng
        if (users.isEmpty()) throw new AppException("NO_USER_IN_ROOM");

        // Lấy thông tin giá điện, nước
        ElectricWaterPricing pricing = electricWaterPricingRepository.latestPricing();
        // Chưa có thông tin giá điện nước -> quản lý cần tạo thông tin giá điện nước trước
        if (pricing == null) throw new AppException("PRICING_NOT_FOUND");

        long electricPrice = index.getElectricIndex() * pricing.getElectricPrice();
        long waterPrice = index.getWaterIndex() * pricing.getWaterPrice();
        long totalPrice = electricPrice + waterPrice;
        // Làm tròn đến hàng ngìn
        long price = Math.round(Math.ceil((double) totalPrice / users.size() / 1000)) * 1000;

        // Tạo hóa đơn
        ElectricWaterBill bill = ElectricWaterBill.builder()
                .price(price)
                .totalPrice(totalPrice)
                .index(index)
                .status(PaymentStatus.PENDING)
                .userCount(users.size())
                .createDate(LocalDateTime.now())
                .build();
        return electricWaterBillRepository.save(bill);
    }

    /**
     * Thông tin chỉ số điện nước của phòng với id
     * @param id id phòng
     * @return thông tin chỉ số điện nước
     */
    @Transactional
    public ElectricWaterIndexResponse getIndexResponseOfRoom(UUID id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        Semester semester = semesterService.getCurrent();
        ElectricWaterIndex index = getIndexOfRoom(room, semester);
        if (index == null) return null;
        return modelMapper.map(index, ElectricWaterIndexResponse.class);
    }

    public ElectricWaterIndex getIndexOfRoom(Room room, Semester semester) {
        ElectricWaterIndex example = new ElectricWaterIndex();
        example.setRoom(room);
        example.setSemester(semester);
        return electricWaterIndexRepository.findOne(Example.of(example)).orElse(null);
    }

    public ElectricWaterBill getBillOfRoom(Room room, Semester semester) {
        return electricWaterBillRepository.findOne((r,q,c) -> c.and(c.equal(r.get("index").get("room"), room), c.equal(r.get("index").get("semester"), semester))).orElse(null);
    }

    public ElectricWaterBill getBillOfIndex(ElectricWaterIndex index) {
        return electricWaterBillRepository.findOne((r,q,c) -> c.equal(r.get("index"), index)).orElse(null);
    }

    /**
     * Lấy thông tin hóa đơn điện nước của phòng
     * @return thông tin hóa đơn điện nước
     */
    public ElectricWaterBillResponse getCurrentBillResponse() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Room room = roomRepository.findByUser(user);
        ElectricWaterBill bill = getBillOfRoom(room, semesterService.getCurrent());
        return modelMapper.map(bill, ElectricWaterBillResponse.class);
    }

    public ElectricWaterBill getBillById(UUID billId) {
        return electricWaterBillRepository.findById(billId).orElse(null);
    }

    public ElectricWaterBill successBill(ElectricWaterBill bill) {
        bill.setStatus(PaymentStatus.SUCCESS);
        return electricWaterBillRepository.save(bill);
    }

    public List<ElectricWaterPricingResponse> getAllPricing() {
        return electricWaterPricingRepository.findAll().stream().map(pricing -> modelMapper.map(pricing, ElectricWaterPricingResponse.class)).toList();
    }

    public ElectricWaterPricingResponse getPricing(UUID id) {
        return modelMapper.map(electricWaterBillRepository.findById(id).orElseThrow(() -> new AppException("PRICING_NOT_FOUND")), ElectricWaterPricingResponse.class);
    }

    public ElectricWaterPricingResponse createPricing(CreateElectricWaterPricingRequest request) {
        ElectricWaterPricing pricing = modelMapper.map(request, ElectricWaterPricing.class);
        pricing.setStartDate(LocalDateTime.now());
        pricing = electricWaterPricingRepository.save(pricing);
        return modelMapper.map(pricing, ElectricWaterPricingResponse.class);
    }

    public ElectricWaterPricingResponse updatePricing(UpdateElectricWaterPricingRequest request) {
        ElectricWaterPricing pricing = modelMapper.map(request, ElectricWaterPricing.class);
        pricing.setStartDate(LocalDateTime.now());
        pricing = electricWaterPricingRepository.save(pricing);
        return modelMapper.map(pricing, ElectricWaterPricingResponse.class);
    }
}
