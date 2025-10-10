package com.capstone.capstone.service.impl;


import com.capstone.capstone.dto.request.news.NewsRequest;
import com.capstone.capstone.dto.response.news.NewsReponse;
import com.capstone.capstone.dto.response.user.ProfileUserResponse;
import com.capstone.capstone.entity.News;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.NewsRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.INewsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsService implements INewsService {

    private final NewsRepository newsRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;


    @Override
    public List<NewsReponse> getAllNews() {
        return newsRepository.findAll().stream()
                .map(p -> modelMapper.map(p, NewsReponse.class))
                .toList();
    }

    @Override
    public NewsReponse getNewsDetail(UUID id) {
        return null;
    }

    @Override
    public NewsReponse createNews(NewsRequest newsRequest) {
        News news = modelMapper.map(newsRequest, News.class);
        User user = userRepository.findById(newsRequest.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        news.setUser(user);
        News savedNews = newsRepository.save(news);
        NewsReponse newsReponse = modelMapper.map(savedNews, NewsReponse.class);
        newsReponse.setName(savedNews.getUser().getUsername());
        return newsReponse;
    }


}
