package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.service.TipBasedTradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tip")
public class TipBasedTradingController {

    @Autowired
    TipBasedTradingService tipBasedTradingService;

    @GetMapping
    public void trade(){
        tipBasedTradingService.tipBasedTrading();
    }
}
