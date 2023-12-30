package com.nextjedi.trading.tipbasedtrading.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeModelServiceTest {
    @Autowired
    private TradeModelService tradeModelService;

    @Test
    void registerNewTrade() {
    }

    @Test
    void getAllActiveTrades() {
    }

    @Test
    void getAllTokens() {
    }

    @Test
    void getOrderByInstrumentToken() {
        var res =tradeModelService.getOrderByInstrumentToken(22015234l);
        Assertions.assertNotNull(res);
    }
}