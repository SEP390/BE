package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveySelect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;
import com.capstone.capstone.dto.response.surveySellect.GetAllAnswerSelectedResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.entity.SurveyQuetionSelected;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.repository.SurveySelectRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveySelectServiceTest {

    @Mock
    private SurveySelectRepository surveySelectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SurveyOptionRepository surveyOptionRepository;

    // hiện tại service không dùng tới nhưng vẫn inject để tránh lỗi
    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @InjectMocks
    private SurveySelectService surveySelectService;

    // =========================================================
    // createQuestionSelected
    // =========================================================

    /**
     * 🎯 TC1: Happy case
     * - User tồn tại
     * - Gửi danh sách option thuộc các câu hỏi KHÁC NHAU
     * - Không chọn trùng 2 option của cùng 1 câu hỏi
     * Kỳ vọng:
     * - Lấy đúng user hiện tại
     * - findById cho từng option id
     * - saveAll được gọi với số phần tử = số câu hỏi distinct
     * - Response trả về đúng ids + hasCompletedSurvey
     */
    @Test
    void createQuestionSelected_shouldCreateSuccessfully_whenDistinctQuestions() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID optId1 = UUID.randomUUID();
            UUID optId2 = UUID.randomUUID();

            SurveyQuestion q1 = new SurveyQuestion();
            q1.setId(UUID.randomUUID());
            q1.setQuestionContent("Q1");

            SurveyQuestion q2 = new SurveyQuestion();
            q2.setId(UUID.randomUUID());
            q2.setQuestionContent("Q2");

            SurveyOption o1 = new SurveyOption();
            o1.setId(optId1);
            o1.setOptionContent("A1");
            o1.setSurveyQuestion(q1);

            SurveyOption o2 = new SurveyOption();
            o2.setId(optId2);
            o2.setOptionContent("B1");
            o2.setSurveyQuestion(q2);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(optId1, optId2));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);
            when(surveyOptionRepository.findById(optId1)).thenReturn(Optional.of(o1));
            when(surveyOptionRepository.findById(optId2)).thenReturn(Optional.of(o2));

            // Act
            CreateQuestionSelectedResponse resp = surveySelectService.createQuestionSelected(req);

            // Assert
            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verify(surveyOptionRepository, times(1)).findById(optId1);
            verify(surveyOptionRepository, times(1)).findById(optId2);

            ArgumentCaptor<Iterable<SurveyQuetionSelected>> captor =
                    ArgumentCaptor.forClass(Iterable.class);
            verify(surveySelectRepository, times(1)).saveAll(captor.capture());

            List<SurveyQuetionSelected> saved =
                    (captor.getValue() instanceof List)
                            ? (List<SurveyQuetionSelected>) captor.getValue()
                            : new ArrayList<>();
            if (saved.isEmpty()) {
                // nếu Iterable không phải List, convert bằng tay
                saved.clear();
                captor.getValue().forEach(saved::add);
            }

            assertEquals(2, saved.size(), "Phải lưu 2 lựa chọn (2 câu hỏi khác nhau)");
            assertTrue(
                    saved.stream().allMatch(s -> s.getUser() == user),
                    "Tất cả lựa chọn phải gắn với user hiện tại"
            );

            assertNotNull(resp);
            assertEquals(List.of(optId1, optId2), resp.getIds());
            assertFalse(resp.isHasCompletedSurvey(), "hasCompletedSurvey phản ánh giá trị từ repo");
        }
    }

    /**
     * 🎯 TC2: User không tồn tại trong DB
     * - getCurrentUserId() trả về id
     * - userRepository.findById() trả Optional.empty()
     * Kỳ vọng:
     * - Ném NotFoundException("User not found")
     * - Không gọi tới surveySelectRepository.hasCompletedSurvey
     * - Không gọi surveyOptionRepository.findById, saveAll
     */
    @Test
    void createQuestionSelected_shouldThrowNotFound_whenUserNotFound() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(UUID.randomUUID()));

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act + Assert
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> surveySelectService.createQuestionSelected(req)
            );

            assertEquals("User not found", ex.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, never()).hasCompletedSurvey(any());
            verify(surveyOptionRepository, never()).findById(any());
            verify(surveySelectRepository, never()).saveAll(any());
        }
    }

    /**
     * 🎯 TC3: Một trong các option id không tồn tại
     * Kỳ vọng:
     * - Ném NotFoundException("Survey option not found")
     * - Các option phía sau không được xử lý
     * - Không gọi saveAll
     */
    @Test
    void createQuestionSelected_shouldThrowNotFound_whenOptionNotFound() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID optId1 = UUID.randomUUID();
            UUID optId2 = UUID.randomUUID();

            SurveyQuestion q1 = new SurveyQuestion();
            q1.setId(UUID.randomUUID());

            SurveyOption o1 = new SurveyOption();
            o1.setId(optId1);
            o1.setSurveyQuestion(q1);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(optId1, optId2));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);
            when(surveyOptionRepository.findById(optId1)).thenReturn(Optional.of(o1));
            when(surveyOptionRepository.findById(optId2)).thenReturn(Optional.empty());

            // Act + Assert
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> surveySelectService.createQuestionSelected(req)
            );

            assertEquals("Survey option not found", ex.getMessage());
            verify(surveyOptionRepository, times(1)).findById(optId1);
            verify(surveyOptionRepository, times(1)).findById(optId2);
            verify(surveySelectRepository, never()).saveAll(any());
        }
    }

    /**
     * 🎯 TC4: User chọn 2 option thuộc CÙNG 1 câu hỏi
     * (tức là multi-select cho 1 question) → không được phép
     *
     * Kỳ vọng:
     * - Ném BadHttpRequestException("Survey option already exists")
     * - Không gọi saveAll
     */
    @Test
    void createQuestionSelected_shouldRejectMultipleOptionsForSameQuestion() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID optId1 = UUID.randomUUID();
            UUID optId2 = UUID.randomUUID();

            SurveyQuestion q1 = new SurveyQuestion();
            q1.setId(UUID.randomUUID());

            SurveyOption o1 = new SurveyOption();
            o1.setId(optId1);
            o1.setSurveyQuestion(q1);

            SurveyOption o2 = new SurveyOption();
            o2.setId(optId2);
            o2.setSurveyQuestion(q1); // cùng câu hỏi

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(optId1, optId2));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);
            when(surveyOptionRepository.findById(optId1)).thenReturn(Optional.of(o1));
            when(surveyOptionRepository.findById(optId2)).thenReturn(Optional.of(o2));

            // Act + Assert
            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.createQuestionSelected(req)
            );

            assertEquals("Survey option already exists", ex.getMessage());
            verify(surveySelectRepository, never()).saveAll(any());
        }
    }

    /**
     * 🎯 TC5 (logic thực tế hơn): Không nên cho gửi danh sách rỗng
     * - request.getOptionIds() = empty list
     * Nghiệp vụ: trả survey mà không chọn gì → vô nghĩa → phải ném lỗi.
     */
    @Test
    void createQuestionSelected_shouldRejectEmptyOptionList_inRealisticRule() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(Collections.emptyList());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            // KHÔNG stub hasCompletedSurvey nữa
            // when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);

            // Act + Assert (mong muốn: không cho submit empty)
            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.createQuestionSelected(req),
                    "Nghiệp vụ thực tế: không nên cho submit survey với danh sách option trống"
            );

            assertTrue(ex.getMessage().toLowerCase().contains("empty")
                            || ex.getMessage().toLowerCase().contains("option"),
                    "Message nên nói rõ không được để danh sách option trống");

            verify(userRepository, times(1)).findById(userId);
            // Không được động vào selectRepo / optionRepo
            verifyNoInteractions(surveySelectRepository, surveyOptionRepository);
        }
    }

    /**
     * TC6: User đã hoàn thành survey → trả về danh sách answer mapped đúng
     * Kịch bản:
     * - hasCompletedSurvey(user) = true
     * - findAllByUser(user) trả về list SurveyQuetionSelected
     * Kỳ vọng:
     * - Mỗi phần tử trong response chứa:
     *   + questionId
     *   + questionContent
     *   + optionSelectedId
     *   + optionSelected
     */
    @Test
    void getAllAnswerSelected_shouldReturnAnswers_whenUserCompletedSurvey() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            SurveyQuestion q1 = new SurveyQuestion();
            q1.setId(UUID.randomUUID());
            q1.setQuestionContent("Bạn có hài lòng không?");

            SurveyOption o1 = new SurveyOption();
            o1.setId(UUID.randomUUID());
            o1.setOptionContent("Có");
            o1.setSurveyQuestion(q1);

            SurveyQuetionSelected s1 = new SurveyQuetionSelected();
            s1.setUser(user);
            s1.setSurveyOption(o1);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(true);
            when(surveySelectRepository.findAllByUser(user)).thenReturn(List.of(s1));

            // Act
            List<GetAllAnswerSelectedResponse> result = surveySelectService.getAllAnswerSelected();

            // Assert
            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verify(surveySelectRepository, times(1)).findAllByUser(user);

            assertNotNull(result);
            assertEquals(1, result.size());

            GetAllAnswerSelectedResponse r = result.get(0);
            assertEquals(q1.getId(), r.getQuestionId());
            assertEquals("Bạn có hài lòng không?", r.getQuestionContent());
            assertEquals(o1.getId(), r.getOptionSelectedId());
            assertEquals("Có", r.getOptionSelected());
        }
    }

    /**
     * 🎯 TC7: User chưa hoàn thành survey
     * Kỳ vọng:
     * - hasCompletedSurvey(user) = false → ném BadHttpRequestException
     * - Không gọi findAllByUser
     */
    @Test
    void getAllAnswerSelected_shouldThrowBadRequest_whenSurveyNotCompleted() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);

            // Act + Assert
            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.getAllAnswerSelected()
            );

            // Message hiện tại là "Survey option not found" (khá confusing nhưng test theo behavior hiện tại)
            assertEquals("Survey option not found", ex.getMessage());
            verify(surveySelectRepository, never()).findAllByUser(any());
        }
    }

    /**
     * 🎯 TC8: User hiện tại không tồn tại trong DB
     * Kỳ vọng:
     * - Ném NotFoundException("User not found")
     * - Không gọi hasCompletedSurvey, findAllByUser
     */
    @Test
    void getAllAnswerSelected_shouldThrowNotFound_whenUserNotFound() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act + Assert
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> surveySelectService.getAllAnswerSelected()
            );

            assertEquals("User not found", ex.getMessage());
            verify(surveySelectRepository, never()).hasCompletedSurvey(any());
            verify(surveySelectRepository, never()).findAllByUser(any());
        }
    }
}