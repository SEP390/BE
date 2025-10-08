package com.capstone.capstone.service.impl;


import com.capstone.capstone.dto.response.news.NewsReponse;
import com.capstone.capstone.dto.response.user.ProfileUserResponse;
import com.capstone.capstone.entity.News;
import com.capstone.capstone.repository.NewsRepository;
import com.capstone.capstone.service.interfaces.INewsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService implements INewsService {

    private final NewsRepository newsRepository;
    private final ModelMapper modelMapper;


    @Override
    public List<NewsReponse> getAllNews() {
        return newsRepository.findAll().stream()
                .map(p -> modelMapper.map(p, NewsReponse.class))
                .toList();
    }


}
