package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.FineBill;
import com.capstone.capstone.entity.Report;
import com.capstone.capstone.entity.ReportFineBill;
import com.capstone.capstone.repository.ReportFineBillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReportFineBillService {
    private final ReportFineBillRepository reportFineBillRepository;

    public ReportFineBill create(Report report, FineBill bill) {
        ReportFineBill reportFineBill = new ReportFineBill();
        reportFineBill.setBill(bill);
        reportFineBill.setReport(report);
        return reportFineBillRepository.save(reportFineBill);
    }
}
