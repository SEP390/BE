package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.news.NewsRequest;
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

    @GetMapping("/news")
    public ResponseEntity<List<NewsResponse>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    @PostMapping("/createNews")
    public ResponseEntity<NewsResponse> createNews(@RequestBody NewsRequest newsRequest) {
        NewsResponse newsReponse =newsService.createNews(newsRequest);
        return ResponseEntity.status(HttpStatus.OK).body(newsReponse);
    }

    @GetMapping("/getnewsdetail/{id}")
    public ResponseEntity<NewsResponse> getNewsDetail(@PathVariable UUID id) {
        NewsResponse newsReponse = newsService.getNewsDetail(id);
        return ResponseEntity.status(HttpStatus.OK).body(newsReponse);
    }

    @PutMapping("/updatenews/{id}")
    public ResponseEntity<NewsResponse> updateNews(@RequestBody NewsRequest newsRequest, @PathVariable UUID id) {
        NewsResponse newsReponse = newsService.updateNews(id, newsRequest);
        return ResponseEntity.status(HttpStatus.OK).body(newsReponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<NewsResponse>> searchNews(@RequestParam String title) {

        return ResponseEntity.ok(newsService.searchNewAndFilter(title));
    }

}
