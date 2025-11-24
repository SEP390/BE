package com.capstone.capstone.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IUploadService {
    String uploadImg(MultipartFile file) throws IOException, InterruptedException;
}
