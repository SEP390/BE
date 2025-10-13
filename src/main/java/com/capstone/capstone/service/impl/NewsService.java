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
        News news = newsRepository.findById(id).orElseThrow(()->new RuntimeException("News not found!"));
        return modelMapper.map(news, NewsReponse.class);
    }

    @Override
    public NewsReponse createNews(NewsRequest newsRequest) {
        if (newsRequest.getTitle() == null || newsRequest.getTitle().trim().isEmpty()) {
            throw new RuntimeException("News title cannot be empty");
        }
        News news = modelMapper.map(newsRequest, News.class);
        User user = userRepository.findById(newsRequest.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        news.setUser(user);

        News savedNews = newsRepository.save(news);
        NewsReponse newsReponse = modelMapper.map(savedNews, NewsReponse.class);
        newsReponse.setUserId(savedNews.getUser().getId());
        newsReponse.setName(savedNews.getUser().getUsername());
        return newsReponse;
    }

    @Override
    public NewsReponse updateNews(UUID newsId, NewsRequest newsRequest) {
        News news = newsRepository.findById(newsId).orElseThrow(()->new RuntimeException("News not found!"));
//        User user = userRepository.findById(newsRequest.getUsersId())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        news.setUser(user);
//        //phải set tay ??? tại sao model co phai set tay ??? cayy vclll
//       dung model thay cho set tung thang 1 anh sa giau 2 thang news và thang request
//        news.setTitle(newsRequest.getTitle());
//        news.setContent(newsRequest.getContent());
        modelMapper.map(newsRequest, news);
        News savedNews = newsRepository.save(news);
        NewsReponse newsReponse = modelMapper.map(savedNews, NewsReponse.class);
        newsReponse.setUserId(savedNews.getUser().getId());
        newsReponse.setName(savedNews.getUser().getUsername());
        return newsReponse;
    }



}
