package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.news.NewsRequest;
import com.capstone.capstone.dto.response.news.NewsReponse;
import com.capstone.capstone.entity.News;

import java.util.List;
import java.util.UUID;

public interface INewsService {
    public List<NewsReponse> getAllNews();
    NewsReponse getNewsDetail(UUID id);
    NewsReponse createNews(NewsRequest newsRequest);
}
