package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.response.news.NewsReponse;
import com.capstone.capstone.entity.News;

import java.util.List;

public interface INewsService {
    public List<NewsReponse> getAllNews();
}
