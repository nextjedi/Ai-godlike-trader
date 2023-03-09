package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instruments")
public class InstrumentController {

    Logger logger = LoggerFactory.getLogger(TokenController.class);
    @Autowired
    InstrumentService instrumentService;

    @PostMapping
    public void insertInstruments(){
        logger.info("Insert instrument Controller");
        instrumentService.insertInstruments();
    }

    @GetMapping
    public InstrumentWrapper searchInstrument(InstrumentQuery instrumentQuery){
        logger.info("Search instrument Controller");
        return instrumentService.findInstrumentWithEarliestExpiry(instrumentQuery);
    }
}
