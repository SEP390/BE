package com.capstone.capstone.controller;

import com.capstone.capstone.service.impl.ElectricWaterBillService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ElectricWaterBillController {
    private final ElectricWaterBillService electricWaterBillService;


}
