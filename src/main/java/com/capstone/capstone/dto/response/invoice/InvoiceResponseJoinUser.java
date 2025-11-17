package com.capstone.capstone.dto.response.invoice;

import com.capstone.capstone.dto.response.user.CoreUserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InvoiceResponseJoinUser extends InvoiceResponse {
    private CoreUserResponse user;
}
