package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.response.news.NewsReponse;
import com.capstone.capstone.entity.News;
import com.capstone.capstone.service.impl.NewsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.USER.USER)
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/news")
    public ResponseEntity<List<NewsReponse>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

}
