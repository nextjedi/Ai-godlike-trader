package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import com.nextjedi.trading.tipbasedtrading.util.Helper;
import com.nextjedi.trading.tipbasedtrading.util.OrderParamUtil;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.nextjedi.trading.tipbasedtrading.util.Constants.TAG;
import static com.zerodhatech.kiteconnect.utils.Constants.*;

@Service
@Slf4j
public class TradeExecutorService {
    @Autowired
    private TradeModelService tradeModelService;

    @Async
    public void handleOrderUpdate(Order order) {
        log.info("handle Order Update");
//        todo fetch the relevant order
        if (order.tag.equals(TAG)) {
            switch (order.status){
                case ORDER_COMPLETE -> log.info("buy order complete");
                case ORDER_OPEN -> log.info("buy order open");
                case ORDER_REJECTED -> log.info("buy order Rejected");
                case ORDER_CANCELLED -> log.info("order canceled");
                case ORDER_LAPSED -> log.info("order lapsed");
                case ORDER_TRIGGER_PENDING -> log.info("trigger pending");
            }

        }
    }

    @Async
    public void onTickHandler(Tick tick) {
        log.info("onTickHandler");
        log.info(String.valueOf(tick.getLastTradedPrice()));
//        todo fetch all the tradeModels
//        todo check if current tick belongs to any trade model
//        todo if empty unsubscribe
//        todo if status is open then check for trigger and place order
//        todo if status is entered then handle trail and update order
//        todo if status is closed then unsubscribe
        var trades =tradeModelService.getAllTrades();
        var tradeOptional = trades.stream().filter(tradeModel -> tradeModel.getInstrument().getInstrument_token() == tick.getInstrumentToken()).findFirst();
        if(tradeOptional.isEmpty()){
//            todo unsubscribe
        }
        var trade = tradeOptional.get();
        switch (trade.getTradeStatus()) {
            case NEW -> log.info("new order");
            case ENTERED -> log.info("order already placed, trailing");
            case COMPLETED -> log.info("order is completed, unsubscribe");
            default -> log.info("weird status");
        }
    }
}
