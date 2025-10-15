package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.news.NewsRequest;
import com.capstone.capstone.dto.response.news.NewsResponse;

import java.util.List;
import java.util.UUID;

public interface INewsService {
     List<NewsResponse> getAllNews();
    NewsResponse getNewsDetail(UUID id);
    NewsResponse createNews(NewsRequest newsRequest);
    NewsResponse updateNews(UUID newsId, NewsRequest newsRequest);
    List<NewsResponse> searchNewAndFilter(String title);
}
