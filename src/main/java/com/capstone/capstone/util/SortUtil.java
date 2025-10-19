package com.capstone.capstone.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Optional;

public class SortUtil {
    public static Sort getSort(Pageable pageable, String ...sorts) {
        return Arrays.stream(sorts).map(sort -> Optional.ofNullable(pageable.getSort().getOrderFor(sort)).map(Sort::by).orElse(Sort.unsorted())).reduce(Sort.unsorted(), Sort::and);
    }
}
