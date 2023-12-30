package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import com.nextjedi.trading.tipbasedtrading.service.connecttoexchange.ZerodhaConnectService;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.nextjedi.trading.tipbasedtrading.util.Constants.MAXIMUM_VALUE_PER_TRADE;
import static com.nextjedi.trading.tipbasedtrading.util.Constants.TAG;
import static com.zerodhatech.kiteconnect.utils.Constants.*;

@Service
@Slf4j
public class TradeExecutorService {
    @Autowired
    private TradeModelService tradeModelService;
    @Autowired
    private ZerodhaConnectService zerodhaConnectService;

    @Async
    public void handleOrderUpdate(Order order) {
        log.info("handle Order Update {}",order);
        log.info(order.status);
//        todo fetch the relevant trade
        if (order.tag.equals(TAG)) {
            switch (order.status){
                case ORDER_COMPLETE -> {
                    log.info("buy order complete");
                    handleOrderCompleted(order);
                }
                case ORDER_OPEN -> {
                    log.info("buy order open");
                }
                case ORDER_REJECTED -> {
                    log.error("buy order Rejected");
                    log.error(order.statusMessage);
                }
                case ORDER_CANCELLED -> {
                    log.info("order canceled");
                    log.error(order.statusMessage);
                }
                case ORDER_LAPSED -> log.info("order lapsed");
                case ORDER_TRIGGER_PENDING -> log.info("trigger pending");
            }

        }
    }
    @Async
    private void handleOrderCompleted(Order order){
//        todo fetch trade by order id
        switch (order.transactionType){
            case TRANSACTION_TYPE_BUY -> {
//                todo place stop loss order
//                todo update trade status if needed
            }
            case TRANSACTION_TYPE_SELL -> {
//                todo verify open positions etc
//                todo update trade status
//                todo unsubscribe
            }
        }
    }

    @Async
    public void onTickHandler(Tick tick) {
        log.info("onTickHandler");
        log.info("tick details {} - {}",tick.getInstrumentToken(),tick.getLastTradedPrice());
        var trade =tradeModelService.getOrderByInstrumentToken(tick.getInstrumentToken());
        if(Objects.isNull(trade)){
            log.warn("no relevant order for current tick {}",tick.getInstrumentToken());
            return;
        }
        switch (trade.getTradeStatus()) {
            case NEW -> {
                log.info("new order {}", trade.getInstrument().getName());
                enterTrade(trade,tick);
            }
            case ENTERED -> {
                log.info("order already placed, trailing");
                modifyTrade(trade,tick);
            }
            case COMPLETED -> {
                log.info("order is completed, unsubscribe");
//                todo unsubscribe and check for any related open order or position
            }
            default -> log.info("weird status");
        }
    }
    @Async
    public void enterTrade(TradeModel tradeModel,Tick tick){
        if(tick.getLastTradedPrice()<tradeModel.getTriggerPrice()*0.95){
            log.info("still less than trigger price: {} :: {}",tradeModel.getTarget(),tick.getLastTradedPrice());
            return;
        }
//        todo check for open order for this instrument
        try {
            var kite = zerodhaConnectService.getKiteConnect();
            var margin = kite.getMargins(EXCHANGE_NFO);
            var bal = Math.min(Double.parseDouble(margin.available.cash),MAXIMUM_VALUE_PER_TRADE);
            var orderParam =OrderParamUtil.createBuyOrder(tradeModel.getInstrument(),tick.getLastTradedPrice(),bal,TRANSACTION_TYPE_BUY,tradeModel.getTag());
            var order =kite.placeOrder(orderParam,VARIETY_REGULAR);
            tradeModel.setTradeStatus(TradeStatus.BUSY);
            tradeModel.setEntryOrder(order);
            tradeModelService.updateTrade(tradeModel);
        } catch (KiteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Async
    public void modifyTrade(TradeModel tradeModel,Tick tick){
//        todo
//        todo fetch order id
        var order =tradeModel.getExitOrder();
        var kite = zerodhaConnectService.getKiteConnect();
        try {
            var orders =kite.getOrderHistory(order.orderId);
        } catch (KiteException | IOException e) {
            throw new RuntimeException(e);
        }
//        todo fetch order details
//        todo update order based on condition
    }
}
