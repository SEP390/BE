package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.news.NewsRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.news.NewsResponse;

import com.capstone.capstone.service.impl.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.USER.USER)
public class NewsController {
    private final NewsService newsService;

    // --- Phương thức GET ALL NEWS ---
    @GetMapping("/news")
    public ResponseEntity<BaseResponse<List<NewsResponse>>> getAllNews() {
        List<NewsResponse> newsList = newsService.getAllNews();
        BaseResponse<List<NewsResponse>> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "Get news list successfully",
                newsList
        );
        return ResponseEntity.ok(response);
    }

    // --- Phương thức CREATE NEWS (POST) ---
    @PostMapping("/createNews")
    public ResponseEntity<BaseResponse<NewsResponse>> createNews(@RequestBody NewsRequest newsRequest) {
        NewsResponse newsResponse = newsService.createNews(newsRequest);
        BaseResponse<NewsResponse> response = new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Create successful news",
                newsResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Phương thức GET NEWS DETAIL (GET by ID) ---
    @GetMapping("/getnewsdetail/{id}")
    public ResponseEntity<BaseResponse<NewsResponse>> getNewsDetail(@PathVariable UUID id) {
        NewsResponse newsResponse = newsService.getNewsDetail(id);
        BaseResponse<NewsResponse> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "Get news details successfully",
                newsResponse
        );
        return ResponseEntity.ok(response);
    }

    // --- Phương thức UPDATE NEWS (PUT) ---
    @PutMapping("/updatenews/{id}")
    public ResponseEntity<BaseResponse<NewsResponse>> updateNews(@RequestBody NewsRequest newsRequest, @PathVariable UUID id) {
        NewsResponse newsResponse = newsService.updateNews(id, newsRequest);
        BaseResponse<NewsResponse> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "News update successful",
                newsResponse
        );
        return ResponseEntity.ok(response);
    }

    // --- Phương thức SEARCH NEWS (GET with RequestParam) ---
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<NewsResponse>>> searchNews(@RequestParam String title) {
        List<NewsResponse> searchResults = newsService.searchNewAndFilter(title);
        BaseResponse<List<NewsResponse>> response = new BaseResponse<>(
                HttpStatus.OK.value(),
                "Search for news successfully",
                searchResults
        );
        return ResponseEntity.ok(response);
    }
}