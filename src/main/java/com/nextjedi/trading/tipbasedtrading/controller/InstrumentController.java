package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instruments")
public class InstrumentController {
    @Autowired
    InstrumentService instrumentService;

    @PostMapping
    public void insertInstruments(){
        instrumentService.insertInstruments();
    }

    @GetMapping
    public InstrumentWrapper searchInstrument(InstrumentQuery instrumentQuery){
        return instrumentService.findInstrument(instrumentQuery);
    }
}
