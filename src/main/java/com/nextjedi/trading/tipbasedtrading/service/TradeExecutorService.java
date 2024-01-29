package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.OrderDetail;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    public void handleOrderUpdate(Order order) {
        log.info("handle Order Update {}",order.toString());
        log.info(order.status);
//        todo fetch the relevant trade
        if (order.tag.equals(TAG)) {
            switch (order.status){
                case ORDER_COMPLETE -> {
                    log.info("buy order complete");
                    handleOrderCompleted(order);
                }
                case ORDER_OPEN -> log.info("buy order open");
                case ORDER_REJECTED -> {
                    if(order.transactionType.equalsIgnoreCase(TRANSACTION_TYPE_BUY)){
                        log.error("buy order Rejected");
                        log.error(order.statusMessage);
                    }else {
                        log.error("sell order rejected");
                        handleSellOrderRejected(order);
                    }
                }
                case ORDER_CANCELLED -> {
                    log.info("order canceled");
                    log.error(order.statusMessage);
                }
                case ORDER_LAPSED -> log.info("order lapsed");
                case ORDER_TRIGGER_PENDING -> log.info("trigger pending");
                default -> throw new IllegalStateException("Unexpected value: " + order.status);
            }

        }
    }

    private void handleSellOrderRejected(Order order) {
//        todo fetch the trade
//        todo check for open position
//        todo update status
    }
    private void handleOrderCompleted(Order order){
        switch (order.transactionType){
            case TRANSACTION_TYPE_BUY -> {
                var trade = tradeModelService.getOrderByEntryOrder(Integer.parseInt(order.orderId));
                if(trade.isPresent()){
                    log.info("buy order completed");
                    trade.get().setTradeStatus(TradeStatus.ENTERED);
                    var instr = trade.get().getInstrument();
                    tradeModelService.updateTrade(trade.get());
                    double price = Double.parseDouble(order.averagePrice)*0.85;
                    double trigger = Double.parseDouble(order.averagePrice)*0.87;
                    price = Helper.tickMultiple(price, instr.tick_size);
                    trigger =Helper.tickMultiple(trigger, instr.tick_size);
                    var orderParam = OrderParamUtil.createSellOrder(
                            trade.get().getInstrument(),price,trigger,trade.get().getInstrument().getLot_size(),TAG);
                    log.info("order param {}", orderParam);
                    try {
                        var kite = zerodhaConnectService.getKiteConnect();
                        var orderRes = kite.placeOrder(orderParam,VARIETY_REGULAR);
                        trade.get().setExitOrder(new OrderDetail(orderRes));
                        tradeModelService.updateTrade(trade.get());
                    } catch (KiteException | IOException e) {
                        log.error("something went wrong {}",e.getMessage());
                        throw new RuntimeException(e);
                    }
                }else {
                    log.error("no trade found for this order");
                }
            }
            case TRANSACTION_TYPE_SELL -> {
                var trade = tradeModelService.getOrderByExitOrder(Integer.parseInt(order.orderId));
                if(trade.isPresent()){
                    log.info("sell order completed");
                    trade.get().setTradeStatus(TradeStatus.COMPLETED);
                    tradeModelService.updateTrade(trade.get());
                }else {
                    log.error("no trade found for this order");
                }
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
//            todo handle unsubscribe
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
    public void enterTrade(TradeModel tradeModel,Tick tick){
        if(tick.getLastTradedPrice()<tradeModel.getTriggerPrice()*0.95){
            log.info("still less than trigger price: {} :: {}",tradeModel.getTarget(),tick.getLastTradedPrice());
            return;
        }
        try {
            var kite = zerodhaConnectService.getKiteConnect();
            var margin = kite.getMargins(EXCHANGE_NFO);
            var bal = Math.min(Double.parseDouble(margin.available.cash),MAXIMUM_VALUE_PER_TRADE);
            var orderParam =OrderParamUtil.createBuyOrder(tradeModel.getInstrument(),tick.getLastTradedPrice(),bal,TRANSACTION_TYPE_BUY,tradeModel.getTag());
            if(Objects.isNull(orderParam)){
                log.error("order param is null, not enough balance");
                return;
            }
            var order =kite.placeOrder(orderParam,VARIETY_REGULAR);
            tradeModel.setTradeStatus(TradeStatus.BUSY);
            tradeModel.setEntryOrder(new OrderDetail(order));
            tradeModelService.updateTrade(tradeModel);
        } catch (KiteException | IOException e) {
            log.error("something went wrong {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Async
    public void modifyTrade(TradeModel tradeModel,Tick tick){
        log.info("modifyTrade");
        var order =tradeModel.getExitOrder();
        var movement = tick.getLastTradedPrice() - order.getPrice();
        var movementPercent =movement*100/order.getPrice();
        if(movement >2 && movementPercent >4){
            log.info("movement is more than 2 or 4%, modifying order");
            var instr = tradeModel.getInstrument();
            var kite = zerodhaConnectService.getKiteConnect();
            try {
                double price = tick.getLastTradedPrice() -(movement*0.5);
                double trigger = tick.getLastTradedPrice() -(movement*0.4);
                price =Helper.tickMultiple(price,instr.tick_size);
                trigger =Helper.tickMultiple(trigger,instr.tick_size);
                OrderParams orderP = OrderParamUtil.createSellOrder(instr, price, trigger, order.getQuantity(),tradeModel.getTag());
                var sellOrder =kite.modifyOrder(String.valueOf(order.getOrderId()),orderP, VARIETY_REGULAR);
                tradeModel.setExitOrder(new OrderDetail(sellOrder));
                tradeModelService.updateTrade(tradeModel);
            } catch (KiteException | IOException e) {
                log.error("something went wrong {}",e.getMessage());
                throw new RuntimeException(e);
            }
        }else {
            log.info("movement is less than 2 or 4%, order not modified");
        }
    }
}
