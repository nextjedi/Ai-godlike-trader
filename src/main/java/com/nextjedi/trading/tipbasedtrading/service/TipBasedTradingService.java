package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.util.Helper;
import com.nextjedi.trading.tipbasedtrading.util.KiteUtil;
import com.nextjedi.trading.tipbasedtrading.util.OrderParamUtil;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnOrderUpdate;
import com.zerodhatech.ticker.OnTicks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.nextjedi.trading.tipbasedtrading.models.Constants.MAXIMUM_VALUE_PER_TRADE;

@Service
@Slf4j
public class TipBasedTradingService {
    final long threeSeconds = 3 * 1000;
    final long delay = 0;
//    actually execute trade
    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private KiteUtil kiteUtil;

    boolean active = false;
    Order buyOrder;
    String tag = "Ap2";
    Order sellOrder;
    double sellPrice = 0;
    boolean orderUpdateFlag = false;
    int count=0;
    int qty=0;

    public void trade(TipModel tipModel) throws IOException, KiteException {
        log.info("instrument" +tipModel.getInstrument().toString() );
        KiteConnect kiteSdk = kiteUtil.connectToKite();
        KiteTicker tickerProvider = new KiteTicker(kiteSdk.getAccessToken(), kiteSdk.getApiKey());
        InstrumentWrapper instr = instrumentService.findInstrumentWithEarliestExpiry(tipModel.getInstrument());
        ArrayList<Long> tokens = new ArrayList<>();
        tokens.add((instr.getInstrument_token()));
        Map<String, LTPQuote> quoteMap = kiteSdk.getLTP(new String[]{String.valueOf(instr.getInstrument_token())});
        var quote = quoteMap.get(String.valueOf(instr.getInstrument_token()));
        float balance = Float.parseFloat(kiteSdk.getMargins(Constants.INSTRUMENTS_SEGMENTS_EQUITY).available.liveBalance);

//        todo: if balance is available
        if(balance < quote.lastPrice* instr.getLot_size()){
            log.warn("Balance not available");
            return;
        }
        log.info("Instruments found and balance available");

        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** Subscribe ticks for token.
                 * By default, all tokens are subscribed for modeQuote.
                 * */
                log.info("Kite connection established");
                OrderParams orderParams = OrderParamUtil.createBuyOrder(instr, tipModel.getPrice(),balance, Constants.ORDER_TYPE_MARKET,tag);

                try {
                    log.info("placing buy order"+orderParams.product+" "+orderParams.price);
                    buyOrder = kiteSdk.placeOrder(orderParams, Constants.VARIETY_REGULAR);
                } catch (KiteException | IOException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
        tickerProvider.setOnOrderUpdateListener(new OnOrderUpdate() {
            @Override
            public void onOrderUpdate(Order order) {
                log.info("order update "+order.orderId);
                if(order.tag.equals(tag)){
                    if(order.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)){
                        try {
                            if(!order.orderId.equals(buyOrder.orderId)){
                                return;
                            }
//                            place sell order and subscribe
                            if(order.status.equals(Constants.ORDER_COMPLETE)){
                                log.info("placing sell order and subscribe");
                                log.info("Bought at "+ instr.getTradingSymbol() + order.averagePrice + order.orderType + order.quantity);
                                buyOrder = order;
                                double price = Double.parseDouble(order.averagePrice)*0.85;
                                double trigger = Double.parseDouble(order.averagePrice)*0.87;
                                price = Helper.tickMultiple(price, instr.tick_size);
                                trigger =Helper.tickMultiple(trigger, instr.tick_size);
                                OrderParams params = OrderParamUtil.createSellOrder(instr,price,trigger, Integer.parseInt(order.quantity),tag);
                                sellOrder =kiteSdk.placeOrder(params,Constants.VARIETY_REGULAR);
                                tickerProvider.setMode(tokens, KiteTicker.modeLTP);
                                tickerProvider.subscribe(tokens);

                            }
                        } catch (KiteException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else if(order.transactionType.equals(Constants.TRANSACTION_TYPE_SELL)){
                        if(order.orderId != sellOrder.orderId){
                            if(order.status.equals(Constants.ORDER_COMPLETE)){
                                log.info(instr.getTradingSymbol() + order.price + order.orderType + order.quantity);
                                tickerProvider.disconnect();
                            }else if(order.status.equals(Constants.ORDER_TRIGGER_PENDING)){
//                                todo: once sell order gets failed or rejected
                                log.info("sell order updated "+ order.price);
                            }else if(order.status.equals(Constants.ORDER_REJECTED)){
                                log.info("order rejected" + order.statusMessage);
                            }
                        }

                    }
                }
            }
        });

        tickerProvider.setOnDisconnectedListener(() -> {
            try {
                log.info(kiteSdk.getMargins().toString());
                tickerProvider.unsubscribe(tokens);
            } catch (KiteException | IOException e) {
                throw new RuntimeException(e);}
        });
        tickerProvider.setOnTickerArrivalListener(new OnTicks() {
            @Override
            public void onTicks(ArrayList<Tick> ticks) {
                log.info("ticks size ", ticks.size());
                if(!ticks.isEmpty()) {
                    for (Tick tick:ticks) {
                        if(tick.getInstrumentToken()== instr.getInstrument_token()){
                            try {
                                var order =kiteSdk.getOrders();
                                for (var o :order){
                                    if(!o.status.equalsIgnoreCase(Constants.ORDER_OPEN)){
                                        continue;
                                    }
                                    float triggerPrice = Float.parseFloat(o.triggerPrice);
                                    var movement = tick.getLastTradedPrice() - triggerPrice;
                                    var movementPercent =movement*100/triggerPrice;
                                    if(movement >2 && movementPercent >4){
                                        log.info("price moved by more than 2 rs and 4%");
                                        double price = tick.getLastTradedPrice() -(movement*0.5);
                                        double trigger = tick.getLastTradedPrice() -(movement*0.4);
                                        price =Helper.tickMultiple(price,instr.tick_size);
                                        trigger =Helper.tickMultiple(trigger,instr.tick_size);
                                        OrderParams orderP = OrderParamUtil.createSellOrder(instr, price, trigger, qty,tag);
                                        log.info(count +" number of times order modified");
                                        log.info("set order update flag to ", false);
                                        sellOrder =kiteSdk.modifyOrder(sellOrder.orderId,orderP,Constants.VARIETY_REGULAR);
                                    }
                                }

                            } catch (KiteException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        });
        tickerProvider.connect();
    }


}
