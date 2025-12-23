package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.request.*;
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
        newRequest.setResponseByEmployeeMessage(null);
        newRequest.setResponseByManagerMessage(null);
        requestRepository.save(newRequest);
        CreateRequestResponse createRequestResponse = new CreateRequestResponse();
        createRequestResponse.setRequestType(newRequest.getRequestType());
        createRequestResponse.setRequestStatus(newRequest.getRequestStatus());
        createRequestResponse.setContent(newRequest.getContent());
        createRequestResponse.setSemesterId(semester.getId());
        createRequestResponse.setCreateTime(newRequest.getCreateTime());
        createRequestResponse.setExecuteTime(newRequest.getExecuteTime());
        createRequestResponse.setRequestId(newRequest.getId());
        createRequestResponse.setUseId(userid);
        createRequestResponse.setUserName(user.getUsername());
        return createRequestResponse;
    }

    @Override
    public UpdateRequestResponse updateRequest(UpdateRequestRequest updateRequestRequest, UUID requestId) {
        Request currentRequest = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == RoleEnum.GUARD || user.getRole() == RoleEnum.TECHNICAL) {
            currentRequest.setResponseByEmployeeMessage(updateRequestRequest.getResponseMessage());
            currentRequest.setRequestStatus(updateRequestRequest.getRequestStatus());
        } else if(user.getRole() == RoleEnum.MANAGER) {
            currentRequest.setResponseByManagerMessage(updateRequestRequest.getResponseMessage());
            currentRequest.setRequestStatus(updateRequestRequest.getRequestStatus());
        } else {
            throw new AccessDeniedException("Access denied");
        }
        if(updateRequestRequest.getRequestStatus().equals(RequestStatusEnum.ACCEPTED) ||updateRequestRequest.getRequestStatus().equals(RequestStatusEnum.REJECTED)) {
            currentRequest.setExecuteTime(LocalDateTime.now());
        }
        requestRepository.save(currentRequest);
        UpdateRequestResponse updateRequestResponse = new UpdateRequestResponse();
        updateRequestResponse.setRequestId(currentRequest.getId());
        updateRequestResponse.setUseId(currentRequest.getUser().getId());
        updateRequestResponse.setRequestStatus(currentRequest.getRequestStatus());
        updateRequestResponse.setExecuteTime(currentRequest.getExecuteTime());
        updateRequestResponse.setResponseMessageByEmployee(currentRequest.getResponseByEmployeeMessage());
        updateRequestResponse.setResponseMessageByManager(currentRequest.getResponseByManagerMessage());
        return updateRequestResponse;
    }

    @Override
    public GetRequestByIdResponse getRequestById(UUID id) {
        Request request = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        User user = request.getUser();
        GetRequestByIdResponse getRequestByIdResponse = new GetRequestByIdResponse();
        getRequestByIdResponse.setRequestId(request.getId());
        getRequestByIdResponse.setRequestType(request.getRequestType());
        getRequestByIdResponse.setContent(request.getContent());
        getRequestByIdResponse.setCreateTime(request.getCreateTime());
        getRequestByIdResponse.setExecuteTime(request.getExecuteTime());
        getRequestByIdResponse.setResponseMessageByEmployee(request.getResponseByEmployeeMessage());
        getRequestByIdResponse.setResponseMessageByManager(request.getResponseByManagerMessage());
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
        List<Request> requests;
        RoleEnum role = user.getRole();
        if (role == RoleEnum.MANAGER || role == RoleEnum.ADMIN) {
            requests = requestRepository.findAll();
        } else if (role == RoleEnum.TECHNICAL) {
            requests = requestRepository.findRequestByRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        } else if (role == RoleEnum.RESIDENT){
            requests = requestRepository.findRequestByUser(user);
        }else if(role == RoleEnum.GUARD || role == RoleEnum.CLEANER){
            Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Employee not found"));
            requests = requestRepository.findAllDormRequestsICanViewOnDay(employee.getId(), LocalDate.now());
        }else {
            throw new AccessDeniedException("Access denied");
        }

        requests = requests.stream()
                .filter(req -> req.getRequestType() != RequestTypeEnum.ANONYMOUS)
                .toList();

        List<GetAllRequestResponse> getAllRequestResponse = requests.stream().map(request -> {
            GetAllRequestResponse requestResponse = new GetAllRequestResponse();
            requestResponse.setRequestId(request.getId());
            requestResponse.setResidentId(request.getUser().getId());
            requestResponse.setResidentName(request.getUser().getFullName());
            requestResponse.setRequestType(request.getRequestType());
            requestResponse.setCreateTime(request.getCreateTime());
            requestResponse.setResponseStatus(request.getRequestStatus());
            requestResponse.setSemesterName(request.getSemester().getName());
            requestResponse.setRoomName(request.getRoomNumber());
            requestResponse.setResponseByEmployee(request.getResponseByEmployeeMessage());
            requestResponse.setResponseByManager(request.getResponseByManagerMessage());
            return  requestResponse;
        }).collect(Collectors.toList());
        return getAllRequestResponse;
    }

    @Override
    public List<GetAllAnonymousRequestResponse> getAllAnonymousRequest() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() != RoleEnum.MANAGER && user.getRole() != RoleEnum.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }
        List<Request> requests = requestRepository.findRequestByRequestType(RequestTypeEnum.ANONYMOUS);
        List<GetAllAnonymousRequestResponse> responses  = new ArrayList<>();
        for (Request request : requests) {
            GetAllAnonymousRequestResponse requestResponse = new GetAllAnonymousRequestResponse();
            requestResponse.setRequestId(request.getId());
            requestResponse.setCreateTime(request.getCreateTime());
            requestResponse.setSemesterName(request.getSemester().getName());
            requestResponse.setContent(request.getContent());
            responses.add(requestResponse);
        }
        return responses;
    }
}
