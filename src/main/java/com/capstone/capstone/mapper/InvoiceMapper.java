package com.capstone.capstone.mapper;

import com.capstone.capstone.dto.response.booking.InvoiceResponse;
import com.capstone.capstone.entity.Invoice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    InvoiceResponse toInvoiceResponse(Invoice invoice);
}
