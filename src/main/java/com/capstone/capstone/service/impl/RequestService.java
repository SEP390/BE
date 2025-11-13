package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllRequestResponse;
import com.capstone.capstone.dto.response.request.GetRequestByIdResponse;
import com.capstone.capstone.dto.response.request.UpdateRequestResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.service.interfaces.IRequestService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService implements IRequestService {
    private final SemesterRepository semesterRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final EmployeeRepository employeeRepository;

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
        newRequest.setExecuteTime(null);
        newRequest.setCreateTime(LocalDateTime.now());
        Slot slot = Optional.ofNullable(slotRepository.findByUser(user))
                .orElseThrow(() -> new NotFoundException("Slot not found"));
        newRequest.setRoomNumber(slot.getRoom().getRoomNumber());
        requestRepository.save(newRequest);
        CreateRequestResponse createRequestResponse = new CreateRequestResponse();
        createRequestResponse.setRequestType(newRequest.getRequestType());
        createRequestResponse.setRequestStatus(newRequest.getRequestStatus());
        createRequestResponse.setContent(newRequest.getContent());
        createRequestResponse.setSemesterId(semester.getId());
        createRequestResponse.setResponseMessage(newRequest.getResponseMessage());
        createRequestResponse.setCreateTime(newRequest.getCreateTime());
        createRequestResponse.setExecuteTime(newRequest.getExecuteTime());
        createRequestResponse.setRequestId(newRequest.getId());
        createRequestResponse.setUseId(userid);
        createRequestResponse.setUserName(user.getUsername());
        return createRequestResponse;
    }

    @Override
    public UpdateRequestResponse updateRequest(UpdateRequestRequest request, UUID id) {
        Request currentRequest = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        currentRequest.setResponseMessage(request.getResponseMessage());
        currentRequest.setRequestStatus(request.getRequestStatus());
        if(request.getRequestStatus().equals(RequestStatusEnum.ACCEPTED) ||request.getRequestStatus().equals(RequestStatusEnum.REJECTED)) {
            currentRequest.setExecuteTime(LocalDateTime.now());
        }
        requestRepository.save(currentRequest);
        UpdateRequestResponse updateRequestResponse = new UpdateRequestResponse();
        updateRequestResponse.setRequestId(currentRequest.getId());
        updateRequestResponse.setRequestStatus(currentRequest.getRequestStatus());
        updateRequestResponse.setResponseMessage(currentRequest.getResponseMessage());
        return updateRequestResponse;
    }

    @Override
    public GetRequestByIdResponse getRequestById(UUID id) {
        Request request = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        User user = request.getUser();
        GetRequestByIdResponse getRequestByIdResponse = new GetRequestByIdResponse();
        getRequestByIdResponse.setRequestId(request.getId());
        getRequestByIdResponse.setRequestType(request.getRequestType());
        getRequestByIdResponse.setResponseMessage(request.getResponseMessage());
        getRequestByIdResponse.setContent(request.getContent());
        getRequestByIdResponse.setCreateTime(request.getCreateTime());
        getRequestByIdResponse.setExecuteTime(request.getExecuteTime());
        getRequestByIdResponse.setResponseMessage(request.getResponseMessage());
        getRequestByIdResponse.setSemesterName(request.getSemester().getName());
        getRequestByIdResponse.setResponseStatus(request.getRequestStatus());
        getRequestByIdResponse.setUserId(user.getId());
        Slot slot = slotRepository.findByUser(user);
        getRequestByIdResponse.setRoomName(slot.getRoom().getRoomNumber());
        getRequestByIdResponse.setStatus(request.getRequestStatus());
        return getRequestByIdResponse;
    }

    @Override
    public List<GetAllRequestResponse> getAllRequest() {
        UUID userid = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userid).orElseThrow(() -> new NotFoundException("User not found"));
        Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Employee not found"));
        List<Request> requests;
        RoleEnum role = user.getRole();
        if (role == RoleEnum.MANAGER || role == RoleEnum.ADMIN) {
            requests = requestRepository.findAll();
        } else if (role == RoleEnum.TECHNICAL) {
            requests = requestRepository.findRequestByRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        } else if (role == RoleEnum.RESIDENT){
            requests = requestRepository.findRequestByUser(user);
        }else if(role == RoleEnum.GUARD || role == RoleEnum.CLEANER){
            requests = requestRepository.findAllDormRequestsICanViewOnDay(employee.getId(), LocalDate.now());
        }else {
            throw new AccessDeniedException("Forbidden");
        }
        List<GetAllRequestResponse> getAllRequestResponse = requests.stream().map(request -> {
            GetAllRequestResponse requestResponse = new GetAllRequestResponse();
            requestResponse.setRequestId(request.getId());
            requestResponse.setUserId(request.getUser().getId());
            requestResponse.setUserName(request.getUser().getUsername());
            requestResponse.setRequestType(request.getRequestType());
            requestResponse.setCreateTime(request.getCreateTime());
            requestResponse.setResponseStatus(request.getRequestStatus());
            requestResponse.setSemesterName(request.getSemester().getName());
            requestResponse.setRoomName(request.getRoomNumber());
            return  requestResponse;
        }).collect(Collectors.toList());
        return getAllRequestResponse;
    }
}
