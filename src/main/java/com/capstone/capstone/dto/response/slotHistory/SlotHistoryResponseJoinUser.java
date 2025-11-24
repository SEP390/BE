package com.capstone.capstone.dto.response.slotHistory;

import com.capstone.capstone.dto.response.user.CoreUserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SlotHistoryResponseJoinUser extends SlotHistoryResponse{
    private CoreUserResponse user;
}
