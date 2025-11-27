package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveyOption.UpdateOptionRequest;
import com.capstone.capstone.dto.response.surveyOption.UpdateOptionResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyOptionServiceTest {

    @Mock
    private SurveyOptionRepository surveyOptionRepository;

    // hiện tại service chưa dùng tới, nhưng vẫn mock để inject tránh lỗi
    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @InjectMocks
    private SurveyOptionService surveyOptionService;

    // 🎯 TC1: Update option thành công khi id tồn tại và content hợp lệ
    // Kỳ vọng:
    // - findById được gọi đúng id
    // - nội dung option được cập nhật
    // - repository.save được gọi
    // - response mapping đúng với entity
    @Test
    void updateOption_shouldUpdateSuccessfully_whenOptionExistsAndContentValid() throws BadRequestException {
        // Arrange
        UUID optionId = UUID.randomUUID();
        SurveyOption existingOption = new SurveyOption();
        existingOption.setId(optionId);
        existingOption.setOptionContent("Cũ");

        UpdateOptionRequest request = new UpdateOptionRequest();
        request.setContent("Mới");

        when(surveyOptionRepository.findById(optionId))
                .thenReturn(Optional.of(existingOption));
        when(surveyOptionRepository.save(any(SurveyOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateOptionResponse response = surveyOptionService.updateOption(optionId, request);

        // Assert
        ArgumentCaptor<SurveyOption> captor = ArgumentCaptor.forClass(SurveyOption.class);
        verify(surveyOptionRepository, times(1)).findById(optionId);
        verify(surveyOptionRepository, times(1)).save(captor.capture());

        SurveyOption saved = captor.getValue();
        assertEquals("Mới", saved.getOptionContent());

        assertNotNull(response);
        assertEquals(optionId, response.getId());
        assertEquals("Mới", response.getOptionContent());
    }

    // 🎯 TC2: Update option với id không tồn tại → ném NotFoundException, không gọi save
    @Test
    void updateOption_shouldThrowNotFound_whenOptionIdNotFound() {
        // Arrange
        UUID optionId = UUID.randomUUID();
        UpdateOptionRequest request = new UpdateOptionRequest();
        request.setContent("Nội dung mới");

        when(surveyOptionRepository.findById(optionId))
                .thenReturn(Optional.empty());

        // Act + Assert
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> surveyOptionService.updateOption(optionId, request)
        );

        assertEquals("Survey option not found", ex.getMessage());
        verify(surveyOptionRepository, times(1)).findById(optionId);
        verify(surveyOptionRepository, never()).save(any());
    }

    // 🎯 TC3: Thực tế nên không cho update với content = null
    // -> Test này thể hiện rule mong muốn: nếu content null thì ném IllegalArgumentException
    // HIỆN TẠI code chưa validate nên test này sẽ FAIL (để lộ bug / thiếu rule)
    @Test
    void updateOption_shouldRejectNullContent_inRealisticBusinessRule() {
        // Arrange
        UUID optionId = UUID.randomUUID();
        SurveyOption existingOption = new SurveyOption();
        existingOption.setId(optionId);
        existingOption.setOptionContent("Cũ");

        UpdateOptionRequest request = new UpdateOptionRequest();
        request.setContent(null);

        when(surveyOptionRepository.findById(optionId))
                .thenReturn(Optional.of(existingOption));

        // Act + Assert (mong muốn về mặt logic thực tế)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyOptionService.updateOption(optionId, request),
                "Option content không nên được phép null"
        );

        assertTrue(ex.getMessage().contains("content") || ex.getMessage().contains("null"),
                "Message nên nói rõ content không được null/empty");
        verify(surveyOptionRepository, times(1)).findById(optionId);
        verify(surveyOptionRepository, never()).save(any());
    }

    // 🎯 TC4: Thực tế cũng nên reject content rỗng/blank ("", "   ")
    // -> Tương tự TC3, test này thể hiện rule mong muốn; hiện tại code vẫn cho qua
    @Test
    void updateOption_shouldRejectBlankContent_inRealisticBusinessRule() {
        // Arrange
        UUID optionId = UUID.randomUUID();
        SurveyOption existingOption = new SurveyOption();
        existingOption.setId(optionId);
        existingOption.setOptionContent("Cũ");

        UpdateOptionRequest request = new UpdateOptionRequest();
        request.setContent("   "); // toàn space

        when(surveyOptionRepository.findById(optionId))
                .thenReturn(Optional.of(existingOption));

        // Act + Assert (mong muốn)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyOptionService.updateOption(optionId, request),
                "Option content rỗng/blank thực tế không nên được chấp nhận"
        );

        assertTrue(ex.getMessage().toLowerCase().contains("empty")
                        || ex.getMessage().toLowerCase().contains("blank")
                        || ex.getMessage().toLowerCase().contains("content"),
                "Message nên nói rõ content không được empty/blank");
        verify(surveyOptionRepository, times(1)).findById(optionId);
        verify(surveyOptionRepository, never()).save(any());
    }

    // 🎯 TC5 (nhẹ nhàng): Nếu content mới giống hệt content cũ → vẫn update & save (hiện tại code cho qua)
    // Test này chủ yếu để đảm bảo không có side-effect lạ
    @Test
    void updateOption_shouldAllowSameContent_noSideEffect() throws BadRequestException {
        // Arrange
        UUID optionId = UUID.randomUUID();
        SurveyOption existingOption = new SurveyOption();
        existingOption.setId(optionId);
        existingOption.setOptionContent("Giữ nguyên");

        UpdateOptionRequest request = new UpdateOptionRequest();
        request.setContent("Giữ nguyên");

        when(surveyOptionRepository.findById(optionId))
                .thenReturn(Optional.of(existingOption));
        when(surveyOptionRepository.save(any(SurveyOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateOptionResponse response = surveyOptionService.updateOption(optionId, request);

        // Assert
        verify(surveyOptionRepository, times(1)).findById(optionId);
        verify(surveyOptionRepository, times(1)).save(existingOption);

        assertEquals("Giữ nguyên", existingOption.getOptionContent());
        assertEquals(optionId, response.getId());
        assertEquals("Giữ nguyên", response.getOptionContent());
    }
}