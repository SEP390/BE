package com.capstone.capstone.repository;

import com.capstone.capstone.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {

}
