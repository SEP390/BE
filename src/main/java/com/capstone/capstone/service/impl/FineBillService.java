package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.FineBill;
import com.capstone.capstone.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.capstone.capstone.repository.FineBillRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class FineBillService {
    private final FineBillRepository fineBillRepository;

    public FineBill create(User user, Long price, String reason) {
        FineBill fineBill = new FineBill();
        fineBill.setPrice(price);
        fineBill.setReason(reason);
        fineBill.setUser(user);
        return fineBillRepository.save(fineBill);
    }

    public Page<FineBill> getByUser(User user, Pageable pageable) {
        return fineBillRepository.findAll((r,q,c) -> {
            return c.equal(r.get("user"), user);
        }, pageable);
    }

    public List<FineBill> getByUser(User user) {
        return fineBillRepository.findAll((r,q,c) -> {
            return c.equal(r.get("user"), user);
        });
    }

}
