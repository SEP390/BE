package com.capstone.capstone.dto.request.invoice;

import com.capstone.capstone.dto.enums.InvoiceType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateInvoiceRequest {
    private CreateInvoiceSubject subject;
    private String reason;
    private Long price;
    private UUID roomId;
    private List<UserId> users;
    private InvoiceType type;

    @Data
    public static class UserId {
        private UUID userId;
    }
}
