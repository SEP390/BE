package com.capstone.capstone.dto.response.electricwater;

import com.capstone.capstone.dto.response.payment.PaymentCoreResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserElectricWaterResponse extends ElectricWaterBillResponse{
    private PaymentCoreResponse payment;
}
