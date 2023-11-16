package com.nextjedi.trading.tipbasedtrading.config;

import com.nextjedi.trading.tipbasedtrading.service.KiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZerodhaConfig {
    @Autowired
    private KiteService kiteService;
    @Bean
    public KiteService getKiteConnection() {
        kiteService.establishTickConnection();
        if(!kiteService.getKiteTicker().isConnectionOpen()){
            kiteService.getKiteTicker().connect();
        }
        return kiteService;
    }
}
