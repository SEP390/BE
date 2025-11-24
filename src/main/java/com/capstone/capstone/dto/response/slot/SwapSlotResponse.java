package com.capstone.capstone.dto.response.slot;

import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
import lombok.Data;

@Data
public class SwapSlotResponse {
    private SlotResponseJoinRoomAndDorm oldSlot;
    private SlotResponseJoinRoomAndDorm newSlot;
    private InvoiceResponse invoice;
    private SlotHistoryResponse slotHistory;
}
