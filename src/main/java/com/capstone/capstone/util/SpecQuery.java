package com.capstone.capstone.util;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
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

    public Specification<T> and() {
        return Specification.allOf(specs);
    }
}
