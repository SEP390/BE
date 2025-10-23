package com.capstone.capstone.dto.response.electricwater;

import com.capstone.capstone.dto.response.payment.PaymentCoreResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserElectricWaterResponse {
    private Boolean paid;
    private ElectricWaterBillResponse bill;
    private PaymentCoreResponse payment;
}
