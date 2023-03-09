package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.service.TipBasedTradingService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/tip")
public class TipBasedTradingController {
    Logger logger = LoggerFactory.getLogger(TokenController.class);
    @Autowired
    TipBasedTradingService tipBasedTradingService;

    @PostMapping
    public void trade(@RequestBody TipModel tipModel){
        try {
            logger.info("tip controller");
            tipBasedTradingService.trade(tipModel);
        } catch (IOException e) {
            logger.error("IO exception"+e.getMessage());
            throw new RuntimeException(e);
        } catch (KiteException e) {
            logger.error("Kite exception "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
