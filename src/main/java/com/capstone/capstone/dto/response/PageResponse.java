package com.capstone.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    int currentPage;
    int pageSize;
    int totalPage;
    int totalCount;
    List<T> data;
}
