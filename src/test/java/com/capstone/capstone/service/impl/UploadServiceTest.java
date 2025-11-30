package com.capstone.capstone.service.impl;

import com.capstone.capstone.service.interfaces.IUploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UploadServiceTest {

    @InjectMocks
    private UploadService uploadService;

    // Không @Mock HttpClient vì nó được tạo qua static method newHttpClient(), sẽ mock bằng MockedStatic

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Gán các config @Value qua reflection (vì là private field)
        setField(uploadService, "endpoint", "https://api.appwrite.io/v1");
        setField(uploadService, "projectId", "project-123");
        setField(uploadService, "apiKey", "api-key-xyz");
        setField(uploadService, "bucketId", "bucket-abc");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = UploadService.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ---------------------------------------------------------------
    // 🎯 TC1: Upload thành công (statusCode = 201, body JSON hợp lệ)
    // Mục tiêu:
    //  - Mock HttpClient.newHttpClient() trả về client giả
    //  - Mock client.send() trả về status 201 + body có "$id"
    //  - Đảm bảo URL trả về đúng format endpoint/bucket/fileId/view?project=...
    //  - Đảm bảo client.send được gọi 1 lần
    // ---------------------------------------------------------------
    @Test
    void uploadImg_shouldReturnCorrectUrl_whenUploadSuccess() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("fake-image".getBytes());

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String fileId = "file-123";
        String jsonBody = "{\"$id\":\"" + fileId + "\"}";

        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn(jsonBody);

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            // Act
            String resultUrl = uploadService.uploadImg(file);

            // Assert
            String expectedUrl = "https://api.appwrite.io/v1/storage/buckets/bucket-abc/files/"
                    + fileId + "/view?project=project-123";

            assertEquals(expectedUrl, resultUrl);

            // Kiểm tra client.send được gọi đúng 1 lần
            verify(mockClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    // ---------------------------------------------------------------
    // 🎯 TC2: Server trả về status khác 201 (ví dụ 400) → RuntimeException("Upload failed: ...")
    // Mục tiêu:
    //  - Không parse JSON nếu status != 201
    //  - Ném RuntimeException với message chứa "Upload failed" + body error
    // ---------------------------------------------------------------
    @Test
    void uploadImg_shouldThrowRuntimeException_whenStatusNot201() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("fake-image".getBytes());

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Bad Request");

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            // Act + Assert
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> uploadService.uploadImg(file)
            );

            assertTrue(ex.getMessage().contains("Upload failed"));
            assertTrue(ex.getMessage().contains("Bad Request"));

            verify(mockClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    // ---------------------------------------------------------------
    // 🎯 TC3: Status = 201 nhưng body JSON không hợp lệ → IOException từ ObjectMapper
    // Mục tiêu:
    //  - Khi JSON không parse được, phương thức ném IOException ra ngoài (không swallow)
    //  - Giúp phát hiện trường hợp Appwrite trả về body lạ
    // ---------------------------------------------------------------
    @Test
    void uploadImg_shouldThrowIOException_whenResponseBodyIsInvalidJson() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("fake-image".getBytes());

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("NOT_JSON");

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            // Act + Assert
            assertThrows(
                    IOException.class,   // JsonProcessingException extends IOException
                    () -> uploadService.uploadImg(file)
            );

            verify(mockClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    // ---------------------------------------------------------------
    // 🎯 TC4: Lỗi network (client.send ném IOException) → uploadImg propagate IOException
    // Mục tiêu:
    //  - Đảm bảo IOException không bị nuốt, mà ném ra ngoài đúng như method khai báo
    // ---------------------------------------------------------------
    @Test
    void uploadImg_shouldPropagateIOException_whenHttpClientSendFails() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("fake-image".getBytes());

        HttpClient mockClient = mock(HttpClient.class);

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new IOException("Network error"));

            // Act + Assert
            IOException ex = assertThrows(
                    IOException.class,
                    () -> uploadService.uploadImg(file)
            );

            assertTrue(ex.getMessage().contains("Network error"));
            verify(mockClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    // ---------------------------------------------------------------
    // 🎯 TC5: file = null → hiện tại code sẽ NullPointerException (chưa có validate)
    // Mục tiêu:
    //  - Phản ánh BUG thực tế: nên validate file null và ném exception rõ ràng hơn
    //  - Test này CHỦ Ý để lộ bug, không sửa code theo test
    // ---------------------------------------------------------------
    @Test
    void uploadImg_shouldThrowNullPointer_whenFileIsNull_currentBug() {
        // Act + Assert
        assertThrows(
                NullPointerException.class,
                () -> {
                    try {
                        uploadService.uploadImg(null);
                    } catch (InterruptedException e) {
                        fail("Should not throw InterruptedException here");
                    }
                }
        );
    }

    // ---------------------------------------------------------------
    // 🎯 TC6: Kiểm tra HTTP Request được build với header multipart & URL chính xác
    // Mục tiêu:
    //  - Đảm bảo:
    //    + URL: endpoint + "/storage/buckets/" + bucketId + "/files"
    //    + Header "Content-Type" có boundary và prefix "multipart/form-data"
    // ---------------------------------------------------------------
    @Test
    void uploadImg_shouldBuildCorrectHttpRequest() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn("fake-image".getBytes());

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"$id\":\"file-xyz\"}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockClient.send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResponse);

            // Act
            uploadService.uploadImg(file);

            // Assert
            HttpRequest builtReq = requestCaptor.getValue();
            assertEquals(
                    URI.create("https://api.appwrite.io/v1/storage/buckets/bucket-abc/files"),
                    builtReq.uri()
            );
            assertEquals("POST", builtReq.method());

            String contentType = builtReq.headers().firstValue("Content-Type").orElse("");
            assertTrue(contentType.startsWith("multipart/form-data; boundary=Boundary-"));
        }
    }
}