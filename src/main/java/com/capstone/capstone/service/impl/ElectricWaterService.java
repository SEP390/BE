package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.electricwater.*;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterIndexResponse;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterPricingResponse;
import com.capstone.capstone.dto.response.electricwater.UserElectricWaterResponse;
import com.capstone.capstone.dto.response.payment.PaymentCoreResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ElectricWaterService {
    private final RoomRepository roomRepository;
    private final SemesterService semesterService;
    private final ElectricWaterIndexRepository electricWaterIndexRepository;
    private final ElectricWaterBillRepository electricWaterBillRepository;
    private final ElectricWaterPricingRepository electricWaterPricingRepository;
    private final UserRepository userRepository;

    private final PaymentService paymentService;
    private final ModelMapper modelMapper;

    @Transactional
    public ElectricWaterIndexResponse createIndexResponse(CreateElectricWaterIndexRequest request) {
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        Semester semester = semesterService.getById(request.getSemesterId());

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
    public ElectricWaterIndexResponse updateIndexResponse(UpdateElectricWaterIndexRequest request) {
        ElectricWaterIndex index = electricWaterIndexRepository.findById(request.getId()).orElseThrow(() -> new AppException("INDEX_NOT_FOUND"));
        index.setElectricIndex(request.getElectricIndex());
        index.setWaterIndex(request.getWaterIndex());
        index.setCreateDate(LocalDateTime.now());
        index = updateIndex(index);
        return modelMapper.map(index, ElectricWaterIndexResponse.class);
    }

    public ElectricWaterIndex updateIndex(ElectricWaterIndex index) {
        if (electricWaterBillRepository.exists((r, q, c) -> c.equal(r.get("index"), index))) {
            throw new AppException("UPDATE_INDEX_EXISTED_BILL");
        }
        return electricWaterIndexRepository.save(index);
    }

    @Transactional
    public ElectricWaterBillResponse createBillResponse(CreateElectricWaterBillRequest request) {
        ElectricWaterIndex index = electricWaterIndexRepository.findById(request.getIndexId()).orElseThrow(() -> new AppException("INDEX_NOT_FOUND"));
        ElectricWaterBill bill = createBill(index);
        return modelMapper.map(bill, ElectricWaterBillResponse.class);
    }

    public ElectricWaterBill createBill(ElectricWaterIndex index) {
        if (electricWaterBillRepository.exists((r, q, c) -> c.equal(r.get("index"), index))) {
            throw new AppException("BILL_EXISTED");
        }
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

    public ElectricWaterIndex getIndexOfRoom(Room room, Semester semester) {
        ElectricWaterIndex example = new ElectricWaterIndex();
        example.setRoom(room);
        example.setSemester(semester);
        return electricWaterIndexRepository.findOne(Example.of(example)).orElse(null);
    }

    public ElectricWaterIndex getIndexById(UUID id) {
        return electricWaterIndexRepository.findById(id).orElse(null);
    }

    /**
     * 1 phòng, 1 kỳ -> 1/0 bill
     * @param room phòng
     * @param semester kỳ
     * @return bill
     */
    public Optional<ElectricWaterBill> getBillOfRoom(Room room, Semester semester) {
        return electricWaterBillRepository.findOne((r,q,c) -> c.and(c.equal(r.get("index").get("room"), room), c.equal(r.get("index").get("semester"), semester)));
    }

    public Page<ElectricWaterBill> getBillsOfRoom(Room room, Pageable pageable) {
        return electricWaterBillRepository.findAll((r,q,c) -> c.equal(r.get("index").get("room"), room), pageable);
    }

    public ElectricWaterBill getBillOfIndex(ElectricWaterIndex index) {
        return electricWaterBillRepository.findOne((r,q,c) -> c.equal(r.get("index"), index)).orElse(null);
    }

    public ElectricWaterBill getBillById(UUID billId) {
        return electricWaterBillRepository.findById(billId).orElse(null);
    }

    @Transactional
    public ElectricWaterBillResponse getIndexBillResponse(UUID id) {
        ElectricWaterIndex index = getIndexById(id);
        if (index == null) throw new AppException("INDEX_NOT_FOUND");
        ElectricWaterBill bill = getBillOfIndex(index);
        if (bill == null) throw new AppException("BILL_NOT_FOUND");
        return modelMapper.map(bill, ElectricWaterBillResponse.class);
    }

    public ElectricWaterBill getBillByIndex(ElectricWaterIndex index) {
        return electricWaterBillRepository.findOne((r, q, c) -> c.equal(r.get("index"), index)).orElse(null);
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

    /**
     * Tạo đường dẫn thanh toán cho hóa đơn điện nước
     * @param billId id hóa đơn
     * @return đường dẫn thanh toán
     */
    public String createPaymentUrl(UUID billId) {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        ElectricWaterBill bill = getBillById(billId);
        return paymentService.createPaymentUrl(user, bill);
    }

    public ElectricWaterPricingResponse getCurrentPricing() {
        return modelMapper.map(electricWaterPricingRepository.latestPricing(), ElectricWaterPricingResponse.class);
    }

    public ElectricWaterIndexResponse getIndexResponse(UUID roomId, UUID semesterId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        Semester semester = semesterService.getById(semesterId);
        ElectricWaterIndex index = getIndexOfRoom(room, semester);
        if (index == null) return null;
        return modelMapper.map(index, ElectricWaterIndexResponse.class);
    }
}
