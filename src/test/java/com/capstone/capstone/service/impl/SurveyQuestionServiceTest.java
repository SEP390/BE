package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.UpdateQuestionRequest;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyOption.GetOptionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetQuestionByIdResponse;
import com.capstone.capstone.dto.response.surveyQuestion.UpdateQuestionResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyQuestionServiceTest {

    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @Mock
    private SurveyOptionRepository surveyOptionRepository;

    @InjectMocks
    private SurveyQuestionService surveyQuestionService;

    private CreateSurveyQuestionRequest validCreateQuestionRequest;

    @BeforeEach
    void setUp() {
        // Chuẩn bị 1 request tạo câu hỏi hợp lệ (có 2 option)
        validCreateQuestionRequest = new CreateSurveyQuestionRequest();
        validCreateQuestionRequest.setQuestionContent("Bạn đánh giá KTX thế nào?");

        List<CreateSurveyOptionRequest> optionRequests = new ArrayList<>();
        CreateSurveyOptionRequest opt1 = new CreateSurveyOptionRequest();
        opt1.setOptionName("Tốt");
        CreateSurveyOptionRequest opt2 = new CreateSurveyOptionRequest();
        opt2.setOptionName("Bình thường");

        optionRequests.add(opt1);
        optionRequests.add(opt2);

        validCreateQuestionRequest.setSurveyOptions(optionRequests);
    }

    // ------------------------------------------------------------
    // createSurveyQuestion
    // ------------------------------------------------------------

    // TC1: Tạo câu hỏi mới với nhiều option → lưu đúng và map response đúng
    @Test
    void createSurveyQuestion_shouldCreateQuestionAndOptionsCorrectly_whenValidRequest() {
        // Arrange
        // giả lập save question (không cần behavior đặc biệt, vì ta chỉ quan tâm field content)
        when(surveyQuestionRepository.save(any(SurveyQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(surveyOptionRepository.save(any(SurveyOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CreateSurveyQuestionResponse resp =
                surveyQuestionService.createSurveyQuestion(validCreateQuestionRequest);

        // Assert: kiểm tra số lần gọi save
        ArgumentCaptor<SurveyQuestion> questionCaptor = ArgumentCaptor.forClass(SurveyQuestion.class);
        verify(surveyQuestionRepository, times(1)).save(questionCaptor.capture());

        SurveyQuestion savedQuestion = questionCaptor.getValue();
        assertEquals("Bạn đánh giá KTX thế nào?", savedQuestion.getQuestionContent());

        // mỗi option save 1 lần
        ArgumentCaptor<SurveyOption> optionCaptor = ArgumentCaptor.forClass(SurveyOption.class);
        verify(surveyOptionRepository, times(2)).save(optionCaptor.capture());

        List<SurveyOption> savedOptions = optionCaptor.getAllValues();
        assertEquals(2, savedOptions.size());
        assertEquals("Tốt", savedOptions.get(0).getOptionContent());
        assertEquals("Bình thường", savedOptions.get(1).getOptionContent());

        // các option phải trỏ về đúng question
        savedOptions.forEach(opt -> assertSame(savedQuestion, opt.getSurveyQuestion()));

        // kiểm tra response mapping
        assertNotNull(resp);
        assertEquals("Bạn đánh giá KTX thế nào?", resp.getQuestionContent());
        assertEquals(2, resp.getSurveyOptions().size());
        List<String> optionStrings = resp.getSurveyOptions().stream()
                .map(CreateSurveyOptionResponse::getSurveyOption)
                .toList();
        assertTrue(optionStrings.contains("Tốt"));
        assertTrue(optionStrings.contains("Bình thường"));
    }

    // TC2: List option rỗng -> phải throw IllegalArgumentException
    @Test
    void createSurveyQuestion_shouldThrowException_whenOptionListEmpty() {
        // Arrange
        CreateSurveyQuestionRequest req = new CreateSurveyQuestionRequest();
        req.setQuestionContent("Câu hỏi không có option");
        req.setSurveyOptions(new ArrayList<>());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyQuestionService.createSurveyQuestion(req)
        );

        assertEquals("Survey question must have at least one option", ex.getMessage());
        verify(surveyQuestionRepository, never()).save(any());
        verify(surveyOptionRepository, never()).save(any());
    }

    // TC3: Tạo câu hỏi với surveyOptions = null → hiện tại code sẽ NPE (test để lộ bug)
    @Test
    void createSurveyQuestion_shouldThrowNullPointer_whenOptionListIsNull() {
        // Arrange
        CreateSurveyQuestionRequest req = new CreateSurveyQuestionRequest();
        req.setQuestionContent("Câu hỏi bị thiếu option");
        req.setSurveyOptions(null); // logic thực tế nên validate, nhưng code hiện tại không



        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> surveyQuestionService.createSurveyQuestion(req)
        );

        // Không được gọi save
        assertEquals("Survey question must have at least one option", ex.getMessage());
        verify(surveyQuestionRepository, never()).save(any());
        verify(surveyOptionRepository, never()).save(any());
    }

    // ------------------------------------------------------------
    // getAllQuestion
    // ------------------------------------------------------------

    // TC4: Không có câu hỏi nào trong DB → trả về list rỗng
    @Test
    void getAllQuestion_shouldReturnEmptyList_whenNoQuestionInDatabase() {
        // Arrange
        when(surveyQuestionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<GetAllQuestionResponse> result = surveyQuestionService.getAllQuestion();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(surveyQuestionRepository, times(1)).findAll();
    }

    // TC5: Có nhiều câu hỏi → map đúng id & content
    @Test
    void getAllQuestion_shouldReturnMappedList_whenQuestionsExist() {
        // Arrange
        SurveyQuestion q1 = new SurveyQuestion();
        q1.setId(UUID.randomUUID());
        q1.setQuestionContent("Câu 1");

        SurveyQuestion q2 = new SurveyQuestion();
        q2.setId(UUID.randomUUID());
        q2.setQuestionContent("Câu 2");

        when(surveyQuestionRepository.findAll()).thenReturn(List.of(q1, q2));

        // Act
        List<GetAllQuestionResponse> result = surveyQuestionService.getAllQuestion();

        // Assert
        assertEquals(2, result.size());

        GetAllQuestionResponse r1 = result.get(0);
        assertEquals(q1.getId(), r1.getId());
        assertEquals("Câu 1", r1.getQuestionContent());

        GetAllQuestionResponse r2 = result.get(1);
        assertEquals(q2.getId(), r2.getId());
        assertEquals("Câu 2", r2.getQuestionContent());

        verify(surveyQuestionRepository, times(1)).findAll();
    }

    // ------------------------------------------------------------
    // getQuestionById
    // ------------------------------------------------------------

    // TC6: Lấy câu hỏi theo id thành công (có options) → map đầy đủ cả options
    @Test
    void getQuestionById_shouldReturnQuestionWithOptions_whenFound() {
        // Arrange
        UUID qId = UUID.randomUUID();
        SurveyQuestion q = new SurveyQuestion();
        q.setId(qId);
        q.setQuestionContent("Bạn ở dorm nào?");

        SurveyOption opt1 = new SurveyOption();
        opt1.setId(UUID.randomUUID());
        opt1.setOptionContent("Dorm A");
        opt1.setSurveyQuestion(q);

        SurveyOption opt2 = new SurveyOption();
        opt2.setId(UUID.randomUUID());
        opt2.setOptionContent("Dorm B");
        opt2.setSurveyQuestion(q);

        List<SurveyOption> options = List.of(opt1, opt2);
        q.setSurveyOptions(options);

        when(surveyQuestionRepository.findById(qId)).thenReturn(Optional.of(q));

        // Act
        GetQuestionByIdResponse resp = surveyQuestionService.getQuestionById(qId);

        // Assert
        assertNotNull(resp);
        assertEquals(qId, resp.getId());
        assertEquals("Bạn ở dorm nào?", resp.getQuestionContent());
        assertEquals(2, resp.getOptions().size());

        GetOptionResponse rOpt1 = resp.getOptions().get(0);
        GetOptionResponse rOpt2 = resp.getOptions().get(1);

        assertEquals(opt1.getId(), rOpt1.getId());
        assertEquals("Dorm A", rOpt1.getOptionContent());

        assertEquals(opt2.getId(), rOpt2.getId());
        assertEquals("Dorm B", rOpt2.getOptionContent());

        verify(surveyQuestionRepository, times(1)).findById(qId);
    }

    // TC7: Lấy câu hỏi theo id nhưng không tồn tại → ném NotFoundException
    @Test
    void getQuestionById_shouldThrowNotFound_whenQuestionNotExist() {
        // Arrange
        UUID qId = UUID.randomUUID();
        when(surveyQuestionRepository.findById(qId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class,
                () -> surveyQuestionService.getQuestionById(qId));

        verify(surveyQuestionRepository, times(1)).findById(qId);
    }

    // ------------------------------------------------------------
    // updateQuestion
    // ------------------------------------------------------------

    // TC8: Update content câu hỏi thành công → save được gọi, response trả content mới
    @Test
    void updateQuestion_shouldUpdateContent_whenQuestionExists() {
        // Arrange
        UUID qId = UUID.randomUUID();
        SurveyQuestion q = new SurveyQuestion();
        q.setId(qId);
        q.setQuestionContent("Cũ");

        UpdateQuestionRequest req = new UpdateQuestionRequest();
        req.setQuestionContent("Mới");

        when(surveyQuestionRepository.findById(qId)).thenReturn(Optional.of(q));
        when(surveyQuestionRepository.save(any(SurveyQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UpdateQuestionResponse resp = surveyQuestionService.updateQuestion(req, qId);

        // Assert
        assertEquals(qId, resp.getId());
        assertEquals("Mới", resp.getQuestionContent());
        assertEquals("Mới", q.getQuestionContent());

        verify(surveyQuestionRepository, times(1)).findById(qId);
        verify(surveyQuestionRepository, times(1)).save(q);
    }

    // TC9: Update nhưng question không tồn tại → NotFoundException
    @Test
    void updateQuestion_shouldThrowNotFound_whenQuestionNotExist() {
        // Arrange
        UUID qId = UUID.randomUUID();
        UpdateQuestionRequest req = new UpdateQuestionRequest();
        req.setQuestionContent("Bất kỳ");

        when(surveyQuestionRepository.findById(qId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class,
                () -> surveyQuestionService.updateQuestion(req, qId));

        verify(surveyQuestionRepository, times(1)).findById(qId);
        verify(surveyQuestionRepository, never()).save(any());
    }

    // ------------------------------------------------------------
    // createSurveyOptionForQuestion
    // ------------------------------------------------------------

    // TC10: Thêm option cho câu hỏi tồn tại → save option với question đúng, response đúng
    @Test
    void createSurveyOptionForQuestion_shouldCreateOption_whenQuestionExists() {
        // Arrange
        UUID qId = UUID.randomUUID();
        SurveyQuestion q = new SurveyQuestion();
        q.setId(qId);
        q.setQuestionContent("Câu hỏi bất kỳ");

        CreateSurveyOptionRequest req = new CreateSurveyOptionRequest();
        req.setOptionName("Lựa chọn mới");

        when(surveyQuestionRepository.findById(qId)).thenReturn(Optional.of(q));
        when(surveyOptionRepository.save(any(SurveyOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CreateSurveyOptionResponse resp =
                surveyQuestionService.createSurveyOptionForQuestion(req, qId);

        // Assert
        ArgumentCaptor<SurveyOption> optionCaptor = ArgumentCaptor.forClass(SurveyOption.class);
        verify(surveyOptionRepository, times(1)).save(optionCaptor.capture());

        SurveyOption savedOption = optionCaptor.getValue();
        assertEquals("Lựa chọn mới", savedOption.getOptionContent());
        assertSame(q, savedOption.getSurveyQuestion());

        assertNotNull(resp);
        assertEquals("Lựa chọn mới", resp.getSurveyOption());

        verify(surveyQuestionRepository, times(1)).findById(qId);
    }

    // TC11: Thêm option cho câu hỏi nhưng questionId không tồn tại → NotFoundException
    @Test
    void createSurveyOptionForQuestion_shouldThrowNotFound_whenQuestionNotExist() {
        // Arrange
        UUID qId = UUID.randomUUID();
        CreateSurveyOptionRequest req = new CreateSurveyOptionRequest();
        req.setOptionName("Lựa chọn");

        when(surveyQuestionRepository.findById(qId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class,
                () -> surveyQuestionService.createSurveyOptionForQuestion(req, qId));

        verify(surveyQuestionRepository, times(1)).findById(qId);
        verify(surveyOptionRepository, never()).save(any());
    }
}