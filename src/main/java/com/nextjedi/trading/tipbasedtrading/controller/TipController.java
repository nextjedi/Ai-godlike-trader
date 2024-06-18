package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TipModelRequest;
import com.nextjedi.trading.tipbasedtrading.service.TipBasedTradingService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/tip")
@Slf4j
public class TipController {
    final
    TipBasedTradingService tipBasedTradingService;

    public TipController(TipBasedTradingService tipBasedTradingService) {
        this.tipBasedTradingService = tipBasedTradingService;
    }

    @PostMapping
    public void trade(@RequestBody TipModelRequest tipModelRequest){
        try {
            log.info("tip controller");
            tipBasedTradingService.trade(tipModelRequest);
        } catch (IOException e) {
            log.error("IO exception{}", e.getMessage());
            throw new RuntimeException(e);
        } catch (KiteException e) {
            log.error("Kite exception {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
