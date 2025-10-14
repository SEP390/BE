package com.capstone.capstone.repository;

import com.capstone.capstone.dto.response.news.NewsResponse;
import com.capstone.capstone.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NewsRepository extends JpaRepository<News, UUID> {

    @Query(
            value = """
        select * from news 
                where title
                like concat('%', :title, '%')
        """, nativeQuery = true
            )
    List<News> findNewsByTitle(@Param("title") String title);
}
