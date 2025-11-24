package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.user.CreateUserRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.user.CreateAccountResponse;
import com.capstone.capstone.service.impl.UploadService;
import com.capstone.capstone.service.interfaces.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class UploadController {
    private final IUploadService iUploadService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> uploadImage(@RequestPart(value = "image", required = false) MultipartFile image) throws IOException, InterruptedException {
        String uploadImg = iUploadService.uploadImg(image);
        BaseResponse<String> baseResponse = new BaseResponse<>();
        baseResponse.setData(uploadImg);
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Upload Successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }
}
