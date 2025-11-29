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
import org.mockito.*;
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

    // hiện tại service chưa dùng surveyQuestionRepository, nhưng vẫn mock vào để inject
    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @InjectMocks
    private SurveySelectService surveySelectService;

    // =========================
    // createQuestionSelected()
    // =========================

    /**
     * 🎯 TC1: Tạo câu trả lời survey lần đầu (hasCompletedSurvey = false) với 2 option thuộc 2 câu hỏi khác nhau
     * Kỳ vọng:
     *  - Lấy đúng current user từ AuthenUtil và DB
     *  - Kiểm tra hasCompletedSurvey(user) = false
     *  - Không deleteAll vì đây là lần đầu
     *  - Với mỗi optionId:
     *      + findById() để load SurveyOption
     *      + Map sang SurveyQuetionSelected, set đúng user & option
     *  - saveAll() đúng số lượng (2)
     *  - Response trả về đúng list optionIds và hasCompletedSurvey = false
     */
    @Test
    void createQuestionSelected_shouldCreateNewSelections_whenFirstTimeSurvey() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID optionId1 = UUID.randomUUID();
            UUID optionId2 = UUID.randomUUID();

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(optionId1, optionId2));

            SurveyQuestion q1 = new SurveyQuestion();
            q1.setId(UUID.randomUUID());
            q1.setQuestionContent("Q1?");

            SurveyQuestion q2 = new SurveyQuestion();
            q2.setId(UUID.randomUUID());
            q2.setQuestionContent("Q2?");

            SurveyOption opt1 = new SurveyOption();
            opt1.setId(optionId1);
            opt1.setOptionContent("A1");
            opt1.setSurveyQuestion(q1);

            SurveyOption opt2 = new SurveyOption();
            opt2.setId(optionId2);
            opt2.setOptionContent("A2");
            opt2.setSurveyQuestion(q2);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);
            when(surveyOptionRepository.findById(optionId1)).thenReturn(Optional.of(opt1));
            when(surveyOptionRepository.findById(optionId2)).thenReturn(Optional.of(opt2));

            ArgumentCaptor<Collection<SurveyQuetionSelected>> saveAllCaptor =
                    ArgumentCaptor.forClass(Collection.class);

            // Act
            CreateQuestionSelectedResponse resp =
                    surveySelectService.createQuestionSelected(req);

            // Assert
            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verify(surveyOptionRepository, times(1)).findById(optionId1);
            verify(surveyOptionRepository, times(1)).findById(optionId2);

            // lần đầu -> không deleteAll
            verify(surveySelectRepository, never()).deleteAll(anyCollection());

            // saveAll được gọi với 2 selection
            verify(surveySelectRepository, times(1)).saveAll(saveAllCaptor.capture());
            Collection<SurveyQuetionSelected> saved = saveAllCaptor.getValue();
            assertEquals(2, saved.size(), "Phải lưu đúng 2 selection");

            // Response mapping đúng
            assertNotNull(resp);
            assertEquals(List.of(optionId1, optionId2), resp.getIds());
            assertFalse(resp.isHasCompletedSurvey(), "Lần đầu làm survey thì hasCompletedSurvey phải là false");
        }
    }

    /**
     * 🎯 TC2: optionIds = null → nghiệp vụ yêu cầu ném BadHttpRequestException
     * Kỳ vọng:
     *  - Vẫn load user + hasCompletedSurvey (do code gọi trước validate)
     *  - Ném BadHttpRequestException với message chứa 'empty' hoặc 'option'
     *  - Không gọi surveyOptionRepository và không saveAll / deleteAll
     */
    @Test
    void createQuestionSelected_shouldRejectNullOptionList() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);

            // Act + Assert
            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.createQuestionSelected(req),
                    "optionIds = null phải bị từ chối"
            );

            assertTrue(ex.getMessage().toLowerCase().contains("empty")
                            || ex.getMessage().toLowerCase().contains("option"),
                    "Message nên nói rõ không được để danh sách option trống");

            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verifyNoInteractions(surveyOptionRepository);
            verify(surveySelectRepository, never()).saveAll(anyCollection());
            verify(surveySelectRepository, never()).deleteAll(anyCollection());
        }
    }

    /**
     * 🎯 TC3: optionIds = empty list → nghiệp vụ giống TC2: không được submit rỗng
     */
    @Test
    void createQuestionSelected_shouldRejectEmptyOptionList() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(Collections.emptyList());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);

            // Act + Assert
            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.createQuestionSelected(req),
                    "optionIds rỗng phải bị từ chối"
            );

            assertTrue(ex.getMessage().toLowerCase().contains("empty")
                            || ex.getMessage().toLowerCase().contains("option"),
                    "Message nên mô tả 'Option list cannot be empty' hoặc tương tự");

            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verifyNoInteractions(surveyOptionRepository);
            verify(surveySelectRepository, never()).saveAll(anyCollection());
            verify(surveySelectRepository, never()).deleteAll(anyCollection());
        }
    }

    /**
     * 🎯 TC4: Một optionId không tồn tại trong DB → ném NotFoundException
     * Kỳ vọng:
     *  - Vẫn kiểm tra hasCompletedSurvey
     *  - Gọi findById(optionId) và trả Optional.empty
     *  - Ném NotFoundException("Survey option not found")
     *  - Không gọi saveAll / deleteAll
     */
    @Test
    void createQuestionSelected_shouldThrowNotFound_whenOptionNotExist() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {

            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID invalidOptionId = UUID.randomUUID();
            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(invalidOptionId));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);
            when(surveyOptionRepository.findById(invalidOptionId)).thenReturn(Optional.empty());

            // Act + Assert
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> surveySelectService.createQuestionSelected(req)
            );

            assertEquals("Survey option not found", ex.getMessage());

            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verify(surveyOptionRepository, times(1)).findById(invalidOptionId);
            verify(surveySelectRepository, never()).saveAll(anyCollection());
            verify(surveySelectRepository, never()).deleteAll(anyCollection());
        }
    }

    /**
     * 🎯 TC5: Chọn 2 option nhưng cùng thuộc 1 câu hỏi → BUSINESS RULE: 1 question chỉ được 1 option
     * Kỳ vọng:
     *  - Lần đầu gặp questionId: thêm vào map
     *  - Lần 2 cùng questionId: ném BadHttpRequestException("Survey option already exists")
     *  - Không lưu gì xuống DB (saveAll/deleteAll)
     */
    @Test
    void createQuestionSelected_shouldRejectDuplicateQuestionSelection() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID optionId1 = UUID.randomUUID();
            UUID optionId2 = UUID.randomUUID();

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(optionId1, optionId2));

            SurveyQuestion question = new SurveyQuestion();
            question.setId(UUID.randomUUID());
            question.setQuestionContent("Một câu hỏi");

            SurveyOption opt1 = new SurveyOption();
            opt1.setId(optionId1);
            opt1.setOptionContent("Option 1");
            opt1.setSurveyQuestion(question);

            SurveyOption opt2 = new SurveyOption();
            opt2.setId(optionId2);
            opt2.setOptionContent("Option 2");
            opt2.setSurveyQuestion(question); // cùng question

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);
            when(surveyOptionRepository.findById(optionId1)).thenReturn(Optional.of(opt1));
            when(surveyOptionRepository.findById(optionId2)).thenReturn(Optional.of(opt2));

            // Act + Assert
            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.createQuestionSelected(req),
                    "Không được phép chọn >1 option cho cùng 1 câu hỏi"
            );

            assertTrue(ex.getMessage().toLowerCase().contains("exists")
                            || ex.getMessage().toLowerCase().contains("already"),
                    "Message nên mô tả duplicate selection");

            // Lần 1 gọi đến option 1, lần 2 gọi option 2
            verify(surveyOptionRepository, times(1)).findById(optionId1);
            verify(surveyOptionRepository, times(1)).findById(optionId2);

            // Không saveAll / deleteAll
            verify(surveySelectRepository, never()).saveAll(anyCollection());
            verify(surveySelectRepository, never()).deleteAll(anyCollection());
        }
    }

    /**
     * 🎯 TC6: User đã làm survey trước đó (hasCompletedSurvey = true) → phải xóa hết selection cũ rồi lưu selection mới
     * Kỳ vọng:
     *  - hasCompletedSurvey(user) = true
     *  - findAllByUser(user) trả về list cũ
     *  - deleteAll(oldList) được gọi
     *  - saveAll(newSelection) được gọi với số lượng đúng
     *  - Response.hasCompletedSurvey = true
     */
    @Test
    void createQuestionSelected_shouldReplaceOldSelections_whenUserHasCompletedSurveyBefore() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {

            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            UUID newOptionId = UUID.randomUUID();

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(newOptionId));

            // question & option mới
            SurveyQuestion newQuestion = new SurveyQuestion();
            newQuestion.setId(UUID.randomUUID());

            SurveyOption newOption = new SurveyOption();
            newOption.setId(newOptionId);
            newOption.setSurveyQuestion(newQuestion);
            newOption.setOptionContent("New");

            // selections cũ
            SurveyQuetionSelected old1 = new SurveyQuetionSelected();
            old1.setId(UUID.randomUUID());
            old1.setUser(user);

            List<SurveyQuetionSelected> oldSelections = List.of(old1);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(true);
            when(surveyOptionRepository.findById(newOptionId)).thenReturn(Optional.of(newOption));
            when(surveySelectRepository.findAllByUser(user)).thenReturn(oldSelections);

            ArgumentCaptor<Collection<SurveyQuetionSelected>> saveAllCaptor =
                    ArgumentCaptor.forClass(Collection.class);

            // Act
            CreateQuestionSelectedResponse resp =
                    surveySelectService.createQuestionSelected(req);

            // Assert
            verify(surveySelectRepository, times(1)).findAllByUser(user);
            verify(surveySelectRepository, times(1)).deleteAll(oldSelections);
            verify(surveySelectRepository, times(1)).saveAll(saveAllCaptor.capture());

            Collection<SurveyQuetionSelected> saved = saveAllCaptor.getValue();
            assertEquals(1, saved.size(), "Phải chỉ lưu 1 selection mới");

            assertNotNull(resp);
            assertEquals(List.of(newOptionId), resp.getIds());
            assertTrue(resp.isHasCompletedSurvey(), "Nếu trước đó đã làm survey thì cờ hasCompletedSurvey phải là true");
        }
    }

    /**
     * 🎯 TC7: User không tồn tại trong DB → phải ném NotFoundException
     */
    @Test
    void createQuestionSelected_shouldThrowNotFound_whenUserNotFound() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {

            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            CreateQuestionSelectedRequest req = new CreateQuestionSelectedRequest();
            req.setOptionIds(List.of(UUID.randomUUID()));

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> surveySelectService.createQuestionSelected(req)
            );

            assertEquals("User not found", ex.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verifyNoInteractions(surveyOptionRepository, surveySelectRepository);
        }
    }

    // =========================
    // getAllAnswerSelected()
    // =========================

    /**
     * 🎯 TC8: Lấy câu trả lời khi user chưa hoàn thành survey → ném BadHttpRequestException
     * Kỳ vọng:
     *  - hasCompletedSurvey(user) = false
     *  - Không gọi findAllByUser
     */
    @Test
    void getAllAnswerSelected_shouldThrowBadRequest_whenSurveyNotCompleted() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {

            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(false);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> surveySelectService.getAllAnswerSelected()
            );

            assertTrue(ex.getMessage().toLowerCase().contains("survey"),
                    "Message nên mô tả chưa hoàn thành survey");

            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verify(surveySelectRepository, never()).findAllByUser(any());
        }
    }

    /**
     * 🎯 TC9: Lấy câu trả lời khi user đã hoàn thành survey → trả list câu hỏi + option đã chọn
     * Kỳ vọng:
     *  - hasCompletedSurvey(user) = true
     *  - findAllByUser(user) trả về list các SurveyQuetionSelected
     *  - Map đúng sang GetAllAnswerSelectedResponse
     */
    @Test
    void getAllAnswerSelected_shouldReturnAnswers_whenSurveyCompleted() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {

            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            User user = new User();
            user.setId(userId);

            SurveyQuestion q = new SurveyQuestion();
            q.setId(UUID.randomUUID());
            q.setQuestionContent("Bạn có hài lòng không?");

            SurveyOption opt = new SurveyOption();
            opt.setId(UUID.randomUUID());
            opt.setOptionContent("Rất hài lòng");
            opt.setSurveyQuestion(q);

            SurveyQuetionSelected selected = new SurveyQuetionSelected();
            selected.setId(UUID.randomUUID());
            selected.setUser(user);
            selected.setSurveyOption(opt);

            List<SurveyQuetionSelected> selectedList = List.of(selected);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(surveySelectRepository.hasCompletedSurvey(user)).thenReturn(true);
            when(surveySelectRepository.findAllByUser(user)).thenReturn(selectedList);

            // Act
            List<GetAllAnswerSelectedResponse> resp =
                    surveySelectService.getAllAnswerSelected();

            // Assert
            assertNotNull(resp);
            assertEquals(1, resp.size());

            GetAllAnswerSelectedResponse r = resp.get(0);
            assertEquals(q.getId(), r.getQuestionId());
            assertEquals(q.getQuestionContent(), r.getQuestionContent());
            assertEquals(opt.getId(), r.getOptionSelectedId());
            assertEquals(opt.getOptionContent(), r.getOptionSelected());

            verify(userRepository, times(1)).findById(userId);
            verify(surveySelectRepository, times(1)).hasCompletedSurvey(user);
            verify(surveySelectRepository, times(1)).findAllByUser(user);
        }
    }

    /**
     * 🎯 TC10: getAllAnswerSelected nhưng user không tồn tại → NotFoundException
     */
    @Test
    void getAllAnswerSelected_shouldThrowNotFound_whenUserNotFound() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {

            UUID userId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> surveySelectService.getAllAnswerSelected()
            );

            assertEquals("User not found", ex.getMessage());
            verify(userRepository, times(1)).findById(userId);
            verifyNoInteractions(surveySelectRepository);
        }
    }
}