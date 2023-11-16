package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.service.InstrumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instruments")
@Slf4j
public class InstrumentController {
    @Autowired
    InstrumentService instrumentService;

    @PostMapping
    public boolean insertInstruments(){
        log.info("Insert instrument Controller");
        return instrumentService.insertInstruments();
    }

    @GetMapping
    public InstrumentWrapper searchInstrument(InstrumentQuery instrumentQuery){
        log.info("Search instrument Controller");
        return instrumentService.findInstrumentWithEarliestExpiry(instrumentQuery);
    }
}
