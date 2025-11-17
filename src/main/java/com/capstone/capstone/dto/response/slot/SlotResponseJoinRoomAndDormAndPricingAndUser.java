package com.capstone.capstone.dto.response.slot;

import com.capstone.capstone.dto.response.user.CoreUserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SlotResponseJoinRoomAndDormAndPricingAndUser extends SlotResponseJoinRoomAndDormAndPricing {
    private CoreUserResponse user;
}
