package com.capstone.capstone.util;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor
public class SpecQuery<T> {
    private final List<Specification<T>> specs = new ArrayList<>();

    public void like(String prop, String value) {
        if (value == null || value.isBlank()) return;
        specs.add(((r, q, c) -> {
            return c.like(r.get(prop), "%" + value.replaceAll("%", "") + "%");
        }));
    }

    public void like(Map<String, Object> source, String prop) {
        if (source.get(prop) == null) return;
        specs.add(((r, q, c) -> {
            return c.like(r.get(prop), "%" + String.valueOf(source.get(prop)).replaceAll("%", "") + "%");
        }));
    }

    public void like(Function<Root<T>, Expression<String>> selector, String value) {
        if (value == null || value.isBlank()) return;
        specs.add(((r, q, c) -> {
            return c.like(selector.apply(r), "%" + value.replaceAll("%", "") + "%");
        }));
    }

    public void equal(Function<Root<T>, Expression<String>> selector, Object value) {
        if (value == null) return;
        specs.add((r, q, c) -> {
            return c.equal(selector.apply(r), value);
        });
    }

    public void equal(String prop, Object value) {
        if (value == null) return;
        specs.add((r, q, c) -> {
            return c.equal(r.get(prop), value);
        });
    }

    public void equal(Map<String, Object> source, String prop) {
        if (source.get(prop) == null) return;
        specs.add((r, q, c) -> {
            return c.equal(r.get(prop), source.get(prop));
        });
    }

    public void equal(Map<String, Object> source, Function<Root<T>, Expression<String>> selector, String prop) {
        if (source.get(prop) == null) return;
        specs.add((r, q, c) -> {
            return c.equal(selector.apply(r), source.get(prop));
        });
    }

    public void addSpec(Specification<T> spec) {
        specs.add(spec);
    }

    public Specification<T> and() {
        return Specification.allOf(specs);
    }

    public void timeBetweenDate(Map<String, Object> source, String time, String dateA, String dateB) {
        if (source.get(dateA) == null) return;
        if (source.get(dateB) == null) return;
        LocalDateTime aStart = ((LocalDate) source.get(dateA)).atStartOfDay();
        LocalDateTime bEnd = ((LocalDate) source.get(dateB)).plusDays(1).atStartOfDay();
        specs.add((r,q,c) -> {
            return c.and(c.greaterThanOrEqualTo(r.get(time), aStart), c.lessThanOrEqualTo(r.get(time), bEnd));
        });
    }

    public void betweenDate(Map<String, Object> source, String date, String dateA, String dateB) {
        if (source.get(dateA) == null) return;
        if (source.get(dateB) == null) return;
        specs.add((r,q,c) -> {
            return c.and(c.greaterThanOrEqualTo(r.get(date), dateA), c.lessThanOrEqualTo(r.get(date), dateB));
        });
    }
}
