package com.capstone.capstone.mapper;

import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.PaymentResponse;
import com.capstone.capstone.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toPaymentResponse(Payment payment);

    @Mappings({
            @Mapping(target = "semesterId", source = "slotHistory.semester.id"),
            @Mapping(target = "semesterName", source = "slotHistory.semester.name"),
            @Mapping(target = "slotName", source = "slotHistory.slot.slotName"),
            @Mapping(target = "slotId", source = "slotHistory.slot.id"),
            @Mapping(target = "roomNumber", source = "slotHistory.slot.room.roomNumber"),
            @Mapping(target = "roomId", source = "slotHistory.slot.room.id"),
            @Mapping(target = "dormName", source = "slotHistory.slot.room.dorm.dormName"),
            @Mapping(target = "dormId", source = "slotHistory.slot.room.dorm.id"),
            @Mapping(target = "floor", source = "slotHistory.slot.room.floor"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "createDate", source = "createDate"),
            @Mapping(target = "price", source = "price")
    })
    BookingHistoryResponse toBookingHistoryResponse(Payment payment);
}
