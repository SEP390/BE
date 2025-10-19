package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllRequestResponse;
import com.capstone.capstone.dto.response.request.GetRequestByIdResponse;
import com.capstone.capstone.dto.response.request.UpdateRequestResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.entity.Request;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.RequestRepository;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IRequestService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService implements IRequestService {
    private final SemesterRepository semesterRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public CreateRequestResponse createRequest(CreateRequestRequest request) {
        UUID userid = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userid).orElseThrow(() -> new NotFoundException("User not found"));
        LocalDate currentDate = LocalDate.now();
        Semester semester = semesterRepository.findSemesterByCurrentDate(currentDate).orElseThrow(() -> new NotFoundException("Semester not found"));
        Request newRequest = new Request();
        newRequest.setUser(user);
        newRequest.setSemester(semester);
        newRequest.setRequestType(request.getRequestType());
        newRequest.setRequestStatus(RequestStatusEnum.PENDING);
        newRequest.setContent(request.getContent());
        newRequest.setCreateTime(LocalDateTime.now());
        requestRepository.save(newRequest);
        CreateRequestResponse createRequestResponse = new CreateRequestResponse();
        createRequestResponse.setRequestType(newRequest.getRequestType());
        createRequestResponse.setRequestStatus(newRequest.getRequestStatus());
        createRequestResponse.setContent(newRequest.getContent());
        createRequestResponse.setSemesterId(semester.getId());
        createRequestResponse.setResponseMessage(newRequest.getResponseMessage());
        createRequestResponse.setCreateTime(newRequest.getCreateTime());
        createRequestResponse.setExecuteTime(newRequest.getExecuteTime());
        return createRequestResponse;
    }

    @Override
    public UpdateRequestResponse updateRequest(UpdateRequestRequest request, UUID id) {
        Request currentRequest = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        currentRequest.setResponseMessage(request.getResponseMessage());
        currentRequest.setRequestStatus(request.getRequestStatus());
        requestRepository.save(currentRequest);
        UpdateRequestResponse updateRequestResponse = new UpdateRequestResponse();
        updateRequestResponse.setRequestId(currentRequest.getId());
        updateRequestResponse.setRequestStatus(currentRequest.getRequestStatus());
        updateRequestResponse.setResponseMessage(currentRequest.getResponseMessage());
        return updateRequestResponse;
    }

    @Override
    public GetRequestByIdResponse getRequestById(UUID id) {
        Request currentRequest = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        GetRequestByIdResponse getRequestByIdResponse = new GetRequestByIdResponse();
        getRequestByIdResponse.setRequestId(currentRequest.getId());
        getRequestByIdResponse.setRequestType(currentRequest.getRequestType());
        getRequestByIdResponse.setResponseMessage(currentRequest.getResponseMessage());
        getRequestByIdResponse.setContent(currentRequest.getContent());
        getRequestByIdResponse.setCreateTime(currentRequest.getCreateTime());
        getRequestByIdResponse.setExecuteTime(currentRequest.getExecuteTime());
        getRequestByIdResponse.setResponseMessage(currentRequest.getResponseMessage());
        getRequestByIdResponse.setSemester(currentRequest.getSemester());
        return getRequestByIdResponse;
    }

    @Override
    public List<GetAllRequestResponse> getAllRequest() {
        List<Request>  requests = requestRepository.findAll();
        List<GetAllRequestResponse> getAllRequestResponse = requests.stream().map(request -> {
            GetAllRequestResponse requestResponse = new GetAllRequestResponse();
            requestResponse.setRequestId(request.getId());
            requestResponse.setRequestType(request.getRequestType());
            requestResponse.setCreateTime(request.getCreateTime());
            requestResponse.setResponseStatus(request.getRequestStatus());
            requestResponse.setSemester(request.getSemester());
            return  requestResponse;
        }).collect(Collectors.toList());
        return getAllRequestResponse;
    }
}
