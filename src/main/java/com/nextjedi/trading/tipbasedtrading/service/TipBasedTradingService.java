package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.util.Helper;
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
import static com.nextjedi.trading.tipbasedtrading.models.Constants.USER_ID;

@Service
@Slf4j
public class TipBasedTradingService {
    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private TokenService tokenService;

    boolean active = false;
    Order buyOrder;
    String tag = "Ap2";
    Order sellOrder;
    double sellPrice = 0;
    boolean orderUpdateFlag = false;
    int count=0;
    int qty=0;
    public KiteConnect connectToKite(){
        var secret = ApiSecret.apiKeys.get(USER_ID);
        TokenAccess tokenAccess=tokenService.getLatestTokenByUserId(USER_ID);
        KiteConnect kiteSdk = new KiteConnect(secret.getApiKey());
        kiteSdk.setAccessToken(tokenAccess.getAccessToken());
        kiteSdk.setPublicToken(tokenAccess.getPublicToken());
        return kiteSdk;
    }

    private OrderParams createBuyOrder(InstrumentWrapper instrumentWrapper,int price,float balance, String orderType ){
        int lotCount = (int) (balance /(price*instrumentWrapper.getLot_size()));
        OrderParams orderParams = new OrderParams();
        orderParams.quantity = instrumentWrapper.getLot_size()*lotCount;
        orderParams.orderType = orderType;
        orderParams.tradingsymbol = instrumentWrapper.getTradingSymbol();
        orderParams.product = Constants.PRODUCT_MIS;
        orderParams.exchange = Constants.EXCHANGE_NFO;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = Double.valueOf(price);
        orderParams.tag = tag;  //tag is optional and it cannot be more than 8 characters and only alphanumeric is allowed
        return orderParams;
    }

    private OrderParams createSellOrder(InstrumentWrapper instrumentWrapper,double price,double trigger, int quantity){
        OrderParams orderParams = new OrderParams();
        orderParams.quantity = quantity;
        orderParams.orderType = Constants.ORDER_TYPE_SL;
        orderParams.tradingsymbol = instrumentWrapper.getTradingSymbol();
        orderParams.product = Constants.PRODUCT_MIS;
        orderParams.exchange = Constants.EXCHANGE_NFO;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = price;
        orderParams.triggerPrice = trigger;
        orderParams.tag = tag;  //tag is optional, and it cannot be more than 8 characters and only alphanumeric is allowed
        return orderParams;
    }
    public void trade(TipModel tipModel) throws IOException, KiteException {
        log.info("instrument" +tipModel.getInstrument().toString() );
        KiteConnect kiteSdk = connectToKite();
        KiteTicker tickerProvider = new KiteTicker(kiteSdk.getAccessToken(), kiteSdk.getApiKey());
        InstrumentWrapper instr = instrumentService.findInstrumentWithEarliestExpiry(tipModel.getInstrument());
        ArrayList<Long> tokens = new ArrayList<>();
        tokens.add((instr.getInstrument_token()));
        Map<String, LTPQuote> quoteMap = kiteSdk.getLTP(new String[]{String.valueOf(instr.getInstrument_token())});
        var quote = quoteMap.get(String.valueOf(instr.getInstrument_token()));
        float balance = Float.parseFloat(kiteSdk.getMargins(Constants.INSTRUMENTS_SEGMENTS_EQUITY).available.liveBalance);

//        todo: if balance is available
        log.info("Instrument " +" "+ instr.getLot_size() +" "+instr.getLast_price()+" "+instr);
        if(balance < quote.lastPrice* instr.getLot_size()){
            log.warn("Balance not available "+ balance);
            return;
        }
        log.info("Instruments found and balance available " +" " + balance +" "+ instr.getLot_size()+" "+ quote.lastPrice);

        tickerProvider.setOnConnectedListener(() -> {
            /** Subscribe ticks for token.
             * By default, all tokens are subscribed for modeQuote.
             * */
            log.info("Ticker connection established");
            OrderParams orderParams = createBuyOrder(instr, tipModel.getPrice(),balance, Constants.ORDER_TYPE_MARKET);

            try {
                log.info("placing buy order "+orderParams.product +" "+orderParams.price);
                buyOrder = kiteSdk.placeOrder(orderParams, Constants.VARIETY_REGULAR);
                log.info("Order placed "+ buyOrder.orderId);
            } catch (KiteException | IOException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        });
        tickerProvider.setOnOrderUpdateListener(order -> {
            log.info("order update "+order.orderId + " " + order.status);
            log.info(order.toString());
            if(order.tag.equals(tag)){
                if(order.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)){
                    try {
                        if(!order.orderId.equals(buyOrder.orderId)){
                            log.info("some other order");
                            return;
                        }
//                            place sell order and subscribe
                        if(order.status.equals(Constants.ORDER_COMPLETE)){
                            log.info("placing sell order and subscribe");
                            log.info("Bought at "+ instr.getTradingSymbol() +"@"+ order.averagePrice+ " "+ order.orderType +" "+ order.quantity);
                            buyOrder = order;
                            double price = Double.parseDouble(order.averagePrice)*0.85;
                            double trigger = Double.parseDouble(order.averagePrice)*0.87;
                            price = Helper.tickMultiple(price, instr.tick_size);
                            trigger =Helper.tickMultiple(trigger, instr.tick_size);
                            OrderParams params =createSellOrder(instr,price,trigger, Integer.parseInt(order.quantity));
                            sellOrder =kiteSdk.placeOrder(params,Constants.VARIETY_REGULAR);
                            log.info("Sell order placed "+ sellOrder.orderId);
                            log.info("subscribe to token");
                            tickerProvider.setMode(tokens, KiteTicker.modeLTP);
                            tickerProvider.subscribe(tokens);

                        }
                    } catch (KiteException | IOException e) {
                        log.error("something went wrong");
                        throw new RuntimeException(e);
                    }
                }else if(order.transactionType.equals(Constants.TRANSACTION_TYPE_SELL)){
                    if(order.orderId.equals(sellOrder.orderId)){
                        if(order.status.equals(Constants.ORDER_COMPLETE)){
                            log.info("sl order complete "+ instr.getTradingSymbol()+"@"+ order.averagePrice+ " "+ order.orderType +" "+ order.quantity);
                            tickerProvider.disconnect();
                        }else if(order.status.equals(Constants.ORDER_TRIGGER_PENDING)){
//                                todo: once sell order gets failed or rejected
                            log.info("sell order updated -> trigger pending "+ order.price);
                            log.info("sl order "+ instr.getTradingSymbol()+"@"+ order.averagePrice+ " "+ order.orderType +" "+ order.quantity);
                        }else if(order.status.equals(Constants.ORDER_REJECTED)){
                            log.error("order rejected" + order.statusMessage);
                            log.error(order.toString());
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
                throw new RuntimeException(e);
            }
        });
        tickerProvider.setOnTickerArrivalListener(ticks -> {
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
                                    OrderParams orderP = createSellOrder(instr, price, trigger, qty);
                                    log.info(" number of times order modified "+count);
                                    log.info("set order update flag to "+ false);
                                    sellOrder =kiteSdk.modifyOrder(sellOrder.orderId,orderP,Constants.VARIETY_REGULAR);
                                }
                            }

                        } catch (KiteException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        tickerProvider.connect();
    }


}
