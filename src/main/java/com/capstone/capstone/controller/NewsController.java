package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.news.NewsRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.news.NewsResponse;

import com.capstone.capstone.service.impl.NewsService; // Giả sử service là NewsService
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // Giả sử ID là UUID

@RestController
@RequiredArgsConstructor
// BASE PATH: /api/news
@RequestMapping(ApiConstant.NEWS.NEWS)
public class NewsController {
    private final NewsService newsService;

    // --- 1. GET ALL NEWS ---
    @GetMapping()
    public ResponseEntity<BaseResponse<List<NewsResponse>>> getAllNews() {
        List<NewsResponse> newsList = newsService.getAllNews();
        BaseResponse<List<NewsResponse>> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "Get news list successfully",
                newsList
        );
        return ResponseEntity.ok(response);
    }

    // --- 2. CREATE NEWS (POST) ---
    @PostMapping()
    public ResponseEntity<BaseResponse<NewsResponse>> createNews(@RequestBody NewsRequest newsRequest) {
        NewsResponse newsResponse = newsService.createNews(newsRequest);
        BaseResponse<NewsResponse> response = new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Create successful news",
                newsResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 3. GET NEWS DETAIL (GET by ID) ---
    @GetMapping(ApiConstant.NEWS.UPDATE)
    public ResponseEntity<BaseResponse<NewsResponse>> getNewsDetail(@PathVariable UUID id) {
        NewsResponse newsResponse = newsService.getNewsDetail(id);
        BaseResponse<NewsResponse> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "Get news details successfully",
                newsResponse
        );
        return ResponseEntity.ok(response);
    }

    // --- 4. UPDATE NEWS (PUT) ---
    @PutMapping(ApiConstant.NEWS.UPDATE)
    public ResponseEntity<BaseResponse<NewsResponse>> updateNews(@RequestBody NewsRequest newsRequest, @PathVariable UUID id) {
        NewsResponse newsResponse = newsService.updateNews(id, newsRequest);
        BaseResponse<NewsResponse> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "News update successful",
                newsResponse
        );
        return ResponseEntity.ok(response);
    }

}