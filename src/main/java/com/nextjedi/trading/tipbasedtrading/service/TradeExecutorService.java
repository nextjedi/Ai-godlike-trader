package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.Call;
import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.util.Helper;
import com.nextjedi.trading.tipbasedtrading.util.OrderParamUtil;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnOrderUpdate;
import com.zerodhatech.ticker.OnTicks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TradeExecutorService {
//    todo define the approach
//    methods my code needs to call
//    place order
//    add instrument to ticker list
//    remove instrument from ticker list
//    todo events that need to be handled
//    on connected
//    on order update
//    on disconnect
//    on tick
//    get/update the trade object
//    todo things scheduler need to do
//    connect
//    disconnect
//    update instrument
    @Autowired
    private KiteService kiteService;
    @Autowired
    private CallService callService;
    private List<Call> calls =new ArrayList<>();
    public void init(){
//        todo schedule
        kiteService.establishTickConnection();
//        get all relevant calls from db
        calls = callService.getAllActiveCalls();
    }

//    todo add a new call
//    add to the list
//    save in db
//    subscribe and place orders
    public void newCall(TipModel tip){
        calls.add(new Call(tip));
    }

//    todo trade a new call
//    analyze call -> check price and current price, check balance that can be allocated,
    private void start(){
        kiteService.getKiteTicker().setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** Subscribe ticks for token.
                 * By default, all tokens are subscribed for modeQuote.
                 * */
                log.info("Kite connection established");
            }
        });
        kiteService.getKiteTicker().setOnOrderUpdateListener(new OnOrderUpdate() {
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

        kiteService.getKiteTicker().setOnDisconnectedListener(() -> {
            try {
                log.info(kiteSdk.getMargins().toString());
                tickerProvider.unsubscribe(tokens);
            } catch (KiteException | IOException e) {
                throw new RuntimeException(e);}
        });
        kiteService.getKiteTicker().setOnTickerArrivalListener(new OnTicks() {
            @Override
            public void onTicks(ArrayList< Tick > ticks) {
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
    }


}
