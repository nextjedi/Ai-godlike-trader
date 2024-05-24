package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.service.connecttoexchange.ZerodhaConnectService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Data
@Service
@Slf4j
@EnableAsync
public class KiteService {

    private final TokenService tokenService;
    private final TradeExecutorService tradeExecutorService;
    private final TradeModelService tradeModelService;
    private final InstrumentService instrumentService;
    private final ZerodhaConnectService zerodhaConnectService;

    public KiteService(TokenService tokenService, TradeExecutorService tradeExecutorService, TradeModelService tradeModelService, InstrumentService instrumentService, ZerodhaConnectService zerodhaConnectService) {
        this.tokenService = tokenService;
        this.tradeExecutorService = tradeExecutorService;
        this.tradeModelService = tradeModelService;
        this.instrumentService = instrumentService;
        this.zerodhaConnectService = zerodhaConnectService;
    }

    public void subscribe(Long token){
        log.info("subscribe to {}",token);
        var kiteTicker = zerodhaConnectService.getKiteTicker();
        if(!kiteTicker.isConnectionOpen()){
            log.error("connection not open");
//            todo throw exception
        }
        var tokens = new ArrayList<Long>();
        tokens.add(token);
        kiteTicker.subscribe(tokens);
    }
    public void unSubscribe(Long token){
        log.info("inside unsubscribe method");
        var kiteTicker = zerodhaConnectService.getKiteTicker();
        var tokens = new ArrayList<Long>();
        tokens.add(token);
        kiteTicker.unsubscribe(tokens);
    }
//    @Scheduled(cron = "0 45 8 * * MON-FRI")
    @PostConstruct
    private void startConnection(){
        log.info("into the connection setup method");
        var kiteTicker = zerodhaConnectService.getKiteTicker();
        kiteTicker.setOnConnectedListener(() -> {
            /** Subscribe ticks for token.
             * By default, all tokens are subscribed for modeQuote.
             * */
            log.info("Kite connection established");
            var instrumentStatus = instrumentService.insertInstruments();
            log.info("Instrument insertion status {}", instrumentStatus);
            kiteTicker.subscribe(tradeModelService.getAllTokens());
        });
        kiteTicker.setOnOrderUpdateListener(order -> {
            log.info("order update " + order.orderId);
            tradeExecutorService.handleOrderUpdate(order);
        });

        kiteTicker.setOnDisconnectedListener(() -> {
            log.info("disconnected");
        });
        kiteTicker.setOnTickerArrivalListener(ticks -> {
            log.info("ticks size {}", ticks.size());
            if(!ticks.isEmpty()) {
                log.info("tick is not empty");
                ticks.forEach(tick -> tradeExecutorService.onTickHandler(tick));
            }
        });
        if(!kiteTicker.isConnectionOpen()){
            log.info("setting up connection");
            kiteTicker.connect();
        }
    }
    @Scheduled(cron = "0 16 16 * * MON-FRI")
    private void endConnection(){
        log.info("disconnect method");
        var kite = zerodhaConnectService.getKiteTicker();
        if(kite.isConnectionOpen()){
            log.info("going to disconnect");
            kite.disconnect();
        }
    }

}
