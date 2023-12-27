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
import java.util.Objects;

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
                case ORDER_OPEN -> {
                    log.info("buy order open");
//                    todo
//                    todo once entry order is place exit/stop loss order
                }
                case ORDER_REJECTED -> {
                    log.error("buy order Rejected");
//                   todo lof the  error
                }
                case ORDER_CANCELLED -> {
                    log.info("order canceled");
                }
                case ORDER_LAPSED -> log.info("order lapsed");
                case ORDER_TRIGGER_PENDING -> log.info("trigger pending");
            }

        }
    }

    @Async
    public void onTickHandler(Tick tick) {
        log.info("onTickHandler");
        log.info("tick details {} - {}",tick.getInstrumentToken(),tick.getLastTradedPrice());
//        todo fetch relevant tradeModel
        var trade =tradeModelService.getOrderByInstrumentToken(tick.getInstrumentToken());
        if(Objects.isNull(trade)){
            log.warn("no relevant order for current tick {}",tick.getInstrumentToken());
//            todo unsubscribe
            return;
        }
//        todo if empty unsubscribe
//        todo if status is open then check for trigger and place order
//        todo if status is entered then handle trail and update order
//        todo if status is closed then unsubscribe

        switch (trade.getTradeStatus()) {
            case NEW -> {
                log.info("new order");
                enterTrade(trade,tick);
            }
            case ENTERED -> {
                log.info("order already placed, trailing");
                modifyTrade(trade,tick);
            }
            case COMPLETED -> {
                log.info("order is completed, unsubscribe");
//                unsubscribe and check for any related open order or position
            }
            default -> log.info("weird status");
        }
    }
    @Async
    private void enterTrade(TradeModel tradeModel,Tick tick){
//        todo
    }
    @Async
    private void modifyTrade(TradeModel tradeModel,Tick tick){
//        todo
    }
}
