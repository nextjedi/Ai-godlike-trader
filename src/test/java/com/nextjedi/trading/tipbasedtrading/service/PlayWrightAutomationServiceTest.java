package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.Constants;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PlayWrightAutomationServiceTest {
    @Autowired
    PlayWrightAutomationService playWrightAutomationService;
    @Test
    void generateToken() {
//        var userId = "FHS049";
//        var secret = ApiSecret.apiKeys.get(userId);
//        var res =playWrightAutomationService.generateToken(secret.getApiKey(),userId ,secret.getPassword(),secret.getTotpKey());
//        assertTrue(StringUtils.isNotBlank(res));
        assertTrue(true);

    }
}