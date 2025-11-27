package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.shift.CreateShiftRequest;
import com.capstone.capstone.dto.request.shift.UpdateShiftRequest;
import com.capstone.capstone.dto.response.shift.CreateShiftResponse;
import com.capstone.capstone.dto.response.shift.GetAllShiftResponse;
import com.capstone.capstone.dto.response.shift.UpdateShiftResponse;
import com.capstone.capstone.entity.Shift;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.ShiftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    // ---------------------------------------------------------
    // createShift
    // ---------------------------------------------------------

    // ✅ TC1: Tạo shift thành công với dữ liệu hợp lệ
    // - Kiểm tra shiftRepository.save được gọi đúng
    // - Entity lưu đúng name, startTime, endTime
    // - Response map đúng với entity
    @Test
    void createShift_shouldCreateSuccessfully_whenValidRequest() {
        // Arrange
        CreateShiftRequest req = new CreateShiftRequest();
        req.setName("Ca sáng");
        req.setStartTime(LocalTime.of(8, 0));
        req.setEndTime(LocalTime.of(12, 0));

        Shift saved = new Shift();
        UUID id = UUID.randomUUID();
        saved.setId(id);
        saved.setName("Ca sáng");
        saved.setStartTime(LocalTime.of(8, 0));
        saved.setEndTime(LocalTime.of(12, 0));

        when(shiftRepository.save(any(Shift.class))).thenReturn(saved);

        // Act
        CreateShiftResponse resp = shiftService.createShift(req);

        // Assert – verify tương tác với repository
        ArgumentCaptor<Shift> captor = ArgumentCaptor.forClass(Shift.class);
        verify(shiftRepository, times(1)).save(captor.capture());
        Shift toSave = captor.getValue();
        assertEquals("Ca sáng", toSave.getName());
        assertEquals(LocalTime.of(8, 0), toSave.getStartTime());
        assertEquals(LocalTime.of(12, 0), toSave.getEndTime());

        // Assert – verify mapping response
        assertNotNull(resp);
        assertEquals(id, resp.getId());
        assertEquals("Ca sáng", resp.getName());
        assertEquals(LocalTime.of(8, 0), resp.getStartTime());
        assertEquals(LocalTime.of(12, 0), resp.getEndTime());
    }

    // ⚠️ TC2 (logic thực tế): Không cho phép tạo shift với name = null/blank
    // Mong muốn: ném IllegalArgumentException, không gọi save.
    // HIỆN TẠI service không validate → test này sẽ FAIL (để lộ bug).
    @Test
    void createShift_shouldRejectNullOrBlankName_inRealisticRule() {
        // Arrange
        CreateShiftRequest req = new CreateShiftRequest();
        req.setName("   ");  // hoặc null cũng không nên chấp nhận
        req.setStartTime(LocalTime.of(8, 0));
        req.setEndTime(LocalTime.of(12, 0));

        // Act + Assert (rule mong muốn)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.createShift(req),
                "Ca trực nên có tên, không nên cho name null/blank"
        );

        assertTrue(
                ex.getMessage().toLowerCase().contains("name")
                        || ex.getMessage().toLowerCase().contains("blank")
                        || ex.getMessage().toLowerCase().contains("null"),
                "Message nên giải thích rõ name không được null/blank"
        );

        verify(shiftRepository, never()).save(any());
    }

    // ⚠️ TC3 (logic thực tế): Không cho phép startTime >= endTime (ca trực thời gian ngược)
    // Mong muốn: ném IllegalArgumentException.
    // HIỆN TẠI service không validate → test này sẽ FAIL (để lộ bug).
    @Test
    void createShift_shouldRejectInvalidTimeRange_inRealisticRule() {
        // Arrange
        CreateShiftRequest req = new CreateShiftRequest();
        req.setName("Ca lỗi");
        req.setStartTime(LocalTime.of(14, 0));
        req.setEndTime(LocalTime.of(12, 0)); // start > end

        // Act + Assert (rule mong muốn)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.createShift(req),
                "Thời gian ca trực startTime phải < endTime"
        );

        assertTrue(
                ex.getMessage().toLowerCase().contains("time")
                        || ex.getMessage().toLowerCase().contains("start")
                        || ex.getMessage().toLowerCase().contains("end"),
                "Message nên nói rõ về khoảng thời gian không hợp lệ"
        );

        verify(shiftRepository, never()).save(any());
    }

    // ---------------------------------------------------------
    // getAllShifts
    // ---------------------------------------------------------

    // ✅ TC4: getAllShifts trả về list rỗng khi DB không có ca nào
    // - Đảm bảo không NPE, repository.findAll được gọi 1 lần.
    @Test
    void getAllShifts_shouldReturnEmptyList_whenNoShiftExists() {
        // Arrange
        when(shiftRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<GetAllShiftResponse> result = shiftService.getAllShifts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(shiftRepository, times(1)).findAll();
    }

    // ✅ TC5: getAllShifts map đúng dữ liệu khi tồn tại nhiều shift
    // - Kiểm tra size list
    // - Kiểm tra từng phần tử được map đúng id/name/start/end
    @Test
    void getAllShifts_shouldReturnMappedList_whenShiftsExist() {
        // Arrange
        Shift s1 = new Shift();
        s1.setId(UUID.randomUUID());
        s1.setName("Ca sáng");
        s1.setStartTime(LocalTime.of(8, 0));
        s1.setEndTime(LocalTime.of(12, 0));

        Shift s2 = new Shift();
        s2.setId(UUID.randomUUID());
        s2.setName("Ca chiều");
        s2.setStartTime(LocalTime.of(13, 0));
        s2.setEndTime(LocalTime.of(17, 0));

        when(shiftRepository.findAll()).thenReturn(List.of(s1, s2));

        // Act
        List<GetAllShiftResponse> result = shiftService.getAllShifts();

        // Assert
        assertEquals(2, result.size());

        GetAllShiftResponse r1 = result.get(0);
        assertEquals(s1.getId(), r1.getId());
        assertEquals("Ca sáng", r1.getName());
        assertEquals(LocalTime.of(8, 0), r1.getStartTime());
        assertEquals(LocalTime.of(12, 0), r1.getEndTime());

        GetAllShiftResponse r2 = result.get(1);
        assertEquals(s2.getId(), r2.getId());
        assertEquals("Ca chiều", r2.getName());
        assertEquals(LocalTime.of(13, 0), r2.getStartTime());
        assertEquals(LocalTime.of(17, 0), r2.getEndTime());

        verify(shiftRepository, times(1)).findAll();
    }

    // ---------------------------------------------------------
    // updateShift
    // ---------------------------------------------------------

    // ✅ TC6: updateShift thành công khi id tồn tại & request hợp lệ
    // - findById được gọi đúng id
    // - Entity được update field đúng
    // - save được gọi
    // - Response map đúng dữ liệu sau update
    @Test
    void updateShift_shouldUpdateSuccessfully_whenShiftExistsAndRequestValid() {
        // Arrange
        UUID id = UUID.randomUUID();
        Shift existing = new Shift();
        existing.setId(id);
        existing.setName("Ca cũ");
        existing.setStartTime(LocalTime.of(8, 0));
        existing.setEndTime(LocalTime.of(12, 0));

        UpdateShiftRequest req = new UpdateShiftRequest();
        req.setName("Ca mới");
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(13, 0));

        when(shiftRepository.findById(id)).thenReturn(Optional.of(existing));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UpdateShiftResponse resp = shiftService.updateShift(id, req);

        // Assert – kiểm tra entity sau update
        ArgumentCaptor<Shift> captor = ArgumentCaptor.forClass(Shift.class);
        verify(shiftRepository, times(1)).findById(id);
        verify(shiftRepository, times(1)).save(captor.capture());

        Shift saved = captor.getValue();
        assertEquals("Ca mới", saved.getName());
        assertEquals(LocalTime.of(9, 0), saved.getStartTime());
        assertEquals(LocalTime.of(13, 0), saved.getEndTime());

        // Response mapping
        assertNotNull(resp);
        assertEquals("Ca mới", resp.getName());
        assertEquals(LocalTime.of(9, 0), resp.getStartTime());
        assertEquals(LocalTime.of(13, 0), resp.getEndTime());
    }

    // ✅ TC7: updateShift với id không tồn tại → ném NotFoundException, không gọi save
    @Test
    void updateShift_shouldThrowNotFound_whenShiftIdNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateShiftRequest req = new UpdateShiftRequest();
        req.setName("Ca mới");
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(13, 0));

        when(shiftRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> shiftService.updateShift(id, req)
        );

        assertEquals("Shift not found", ex.getMessage());
        verify(shiftRepository, times(1)).findById(id);
        verify(shiftRepository, never()).save(any());
    }

    // ⚠️ TC8 (logic thực tế): Không cho update shift với name null/blank
    // Mong muốn: ném IllegalArgumentException, không gọi save.
    // HIỆN TẠI service chưa validate → test này sẽ FAIL (để lộ bug).
    @Test
    void updateShift_shouldRejectNullOrBlankName_inRealisticRule() {
        // Arrange
        UUID id = UUID.randomUUID();
        Shift existing = new Shift();
        existing.setId(id);
        existing.setName("Ca cũ");
        existing.setStartTime(LocalTime.of(8, 0));
        existing.setEndTime(LocalTime.of(12, 0));

        UpdateShiftRequest req = new UpdateShiftRequest();
        req.setName("   "); // blank
        req.setStartTime(LocalTime.of(8, 0));
        req.setEndTime(LocalTime.of(12, 0));

        when(shiftRepository.findById(id)).thenReturn(Optional.of(existing));

        // Act + Assert (rule mong muốn)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.updateShift(id, req),
                "Không nên cho phép update shift với name null/blank"
        );

        assertTrue(ex.getMessage().toLowerCase().contains("name"));
        verify(shiftRepository, times(1)).findById(id);
        verify(shiftRepository, never()).save(any());
    }

    // ⚠️ TC9 (logic thực tế): Không cho update shift với startTime >= endTime
    // Mong muốn: ném IllegalArgumentException.
    // HIỆN TẠI service chưa validate → test này sẽ FAIL (để lộ bug).
    @Test
    void updateShift_shouldRejectInvalidTimeRange_inRealisticRule() {
        // Arrange
        UUID id = UUID.randomUUID();
        Shift existing = new Shift();
        existing.setId(id);
        existing.setName("Ca cũ");
        existing.setStartTime(LocalTime.of(8, 0));
        existing.setEndTime(LocalTime.of(12, 0));

        UpdateShiftRequest req = new UpdateShiftRequest();
        req.setName("Ca lỗi");
        req.setStartTime(LocalTime.of(14, 0));
        req.setEndTime(LocalTime.of(10, 0)); // start > end

        when(shiftRepository.findById(id)).thenReturn(Optional.of(existing));

        // Act + Assert (rule mong muốn)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.updateShift(id, req),
                "Thời gian ca trực sau update cũng phải hợp lệ (start < end)"
        );

        assertTrue(
                ex.getMessage().toLowerCase().contains("time")
                        || ex.getMessage().toLowerCase().contains("start")
                        || ex.getMessage().toLowerCase().contains("end")
        );

        verify(shiftRepository, times(1)).findById(id);
        verify(shiftRepository, never()).save(any());
    }
}