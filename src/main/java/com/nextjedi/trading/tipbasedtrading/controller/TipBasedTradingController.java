package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.service.TipBasedTradingService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/tip")
public class TipBasedTradingController {

    @Autowired
    TipBasedTradingService tipBasedTradingService;

    @PostMapping
    public void trade(TipModel tipModel){

        try {
            tipBasedTradingService.tipBasedTrading(tipModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (KiteException e) {
            System.out.println(e.message);
        }
    }

}
