package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.auth.AuthRequest;
import com.capstone.capstone.dto.request.news.NewsRequest;
import com.capstone.capstone.dto.response.auth.AuthResponse;
import com.capstone.capstone.dto.response.news.NewsResponse;
import com.capstone.capstone.entity.News;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.NewsRepository;
import com.capstone.capstone.repository.UserRepository;

import com.capstone.capstone.service.interfaces.IAuthService;
import com.capstone.capstone.service.interfaces.INewsService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsService implements INewsService {

    private final NewsRepository newsRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;



    @Override
    public List<NewsResponse> getAllNews() {
        return newsRepository.findAll(Sort.by(Sort.Order.desc("date"),Sort.Order.desc("time"))).stream()
                .map(news -> {
                            NewsResponse newsReponse = modelMapper.map(news, NewsResponse.class);
                            newsReponse.setUserNames(news.getUser().getUsername());
                            return newsReponse;
                        }
                ).toList();
    }

    @Override
    public NewsResponse getNewsDetail(UUID id) {
        News news = newsRepository.findById(id).orElseThrow(()->new RuntimeException("News not found!"));
        NewsResponse newsReponse = modelMapper.map(news, NewsResponse.class);
        newsReponse.setUserNames(news.getUser().getUsername());
        return newsReponse;
    }

    @Override
    public NewsResponse createNews(NewsRequest newsRequest) {
        if (newsRequest.getTitle() == null || newsRequest.getTitle().trim().isEmpty()) {
            throw new RuntimeException("News title cannot be empty");
        }
        News news = modelMapper.map(newsRequest, News.class);
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        news.setUser(user);
        News savedNews = newsRepository.save(news);
        NewsResponse newsReponse = modelMapper.map(savedNews, NewsResponse.class);
        newsReponse.setUserNames(savedNews.getUser().getUsername());
        return newsReponse;

    }

    @Override
    public NewsResponse updateNews(UUID newsId, NewsRequest newsRequest) {
        News news = newsRepository.findById(newsId).orElseThrow(()->new RuntimeException("News not found!"));
        modelMapper.map(newsRequest, news);
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        news.setUser(user);
        News savedNews = newsRepository.save(news);
        NewsResponse newsReponse = modelMapper.map(savedNews, NewsResponse.class);
        newsReponse.setUserNames(savedNews.getUser().getUsername());
        return newsReponse;
    }

    @Override
    public List<NewsResponse> searchNewAndFilter(String title) {
        return newsRepository.findNewsByTitle(title).stream()
                .map(news -> {
                    NewsResponse newsReponse = modelMapper.map(news, NewsResponse.class);
                    newsReponse.setUserNames(news.getUser().getUsername());
                    return newsReponse;
                } )
                .toList();
    }



}
