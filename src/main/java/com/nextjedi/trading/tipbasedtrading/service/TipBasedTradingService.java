package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.controller.TokenController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class TipBasedTradingService {
    final long threeSeconds = 3 * 1000;
    final long delay = 0;

    Logger logger = LoggerFactory.getLogger(TokenController.class);
//    actually execute trade
    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private TokenService tokenService;

    boolean active = false;
    Order buyOrder;
    String tag = "Ap2";
    int balance =12000;
    Order sellOrder;
    double sellPrice = 0;
    int qty=0;
    public KiteConnect connectToKite(){
        String apikey = "2himf7a1ff5edpjy";
        String apiSecret = "87mebxtvu3226igmjnkjfjfcrgiphfxb";
        TokenAccess tokenAccess=tokenService.getToken();
        KiteConnect kiteSdk = new KiteConnect(apikey);
        kiteSdk.setAccessToken(tokenAccess.getAccesstoken());
        kiteSdk.setPublicToken(tokenAccess.getPublicToken());
        return kiteSdk;
    }

    private OrderParams createBuyOrder(InstrumentWrapper instrumentWrapper,int price, String orderType ){
        int lotCount = balance /(price*instrumentWrapper.getLot_size());
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
        double lotCount = balance /(price*instrumentWrapper.getLot_size());
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
        orderParams.tag = tag;  //tag is optional and it cannot be more than 8 characters and only alphanumeric is allowed
        return orderParams;
    }
    public void trade(TipModel tipModel) throws IOException, KiteException {
        logger.info("instrument" +tipModel.getInstrument().toString() );
        KiteConnect kiteSdk = connectToKite();
        KiteTicker tickerProvider = new KiteTicker(kiteSdk.getAccessToken(), kiteSdk.getApiKey());
        InstrumentWrapper instr = instrumentService.findInstrument(tipModel.getInstrument());
        ArrayList<Long> tokens = new ArrayList<>();
        tokens.add((instr.getInstrument_token()));
        Map<String, Quote> quoteMap = kiteSdk.getQuote(new String[]{String.valueOf(instr.getInstrument_token())});
        Quote quote = quoteMap.get(String.valueOf(instr.getInstrument_token()));
        float balance = Float.parseFloat(kiteSdk.getMargins(Constants.INSTRUMENTS_SEGMENTS_EQUITY).available.liveBalance);

//        todo: if balance is available
        if(balance < quote.lastPrice* instr.getLot_size()){
            logger.warn("Balance not available");
            return;
        }
        logger.info("Instruments found and balance available");
        String orderType;
        if(quote.lastPrice < tipModel.getPrice())
            orderType = Constants.ORDER_TYPE_LIMIT;
        else if(quote.lastPrice< tipModel.getPrice() * 1.02){
            orderType = Constants.ORDER_TYPE_MARKET;
        }else {
            logger.info("price already moved - call"+tipModel.getPrice()+" current price"+ quote.lastPrice);
            return;
        }
        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** Subscribe ticks for token.
                 * By default, all tokens are subscribed for modeQuote.
                 * */
                logger.info("Kite connection established");
                OrderParams orderParams = createBuyOrder(instr, tipModel.getPrice(),orderType);
                try {
                    logger.info("placing buy order"+orderParams.product+" "+orderParams.price);
                    buyOrder = kiteSdk.placeOrder(orderParams, Constants.VARIETY_REGULAR);
                } catch (KiteException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
        tickerProvider.setOnOrderUpdateListener(new OnOrderUpdate() {
            @Override
            public void onOrderUpdate(Order order) {
                logger.info("order update "+order.orderId);
                if(order.tag.equals(tag)){
                    if(order.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)){
                        try {
                            if(!order.orderId.equals(buyOrder.orderId)){
                                return;
                            }
//                            place sell order and subscribe
                            if(order.status.equals(Constants.ORDER_COMPLETE)){
                                logger.info("placing sell order and subscribe");
                                logger.info("Bought at "+ instr.getTradingSymbol() + order.price + order.orderType + order.quantity);
                                buyOrder = order;
                                double price = Double.parseDouble(order.averagePrice)*0.93;
                                double trigger = Double.parseDouble(order.averagePrice)*0.95;
//                                todo: move to helper method
                                price = Helper.tickMultiple(price, instr.tick_size);
                                trigger =Helper.tickMultiple(trigger, instr.tick_size);
                                OrderParams params =createSellOrder(instr,price,trigger, Integer.parseInt(order.quantity));
                                sellOrder =kiteSdk.placeOrder(params,Constants.VARIETY_REGULAR);
                                sellPrice =Double.parseDouble(order.averagePrice);
                                qty = params.quantity;
                                tickerProvider.setMode(tokens, KiteTicker.modeLTP);
                                tickerProvider.subscribe(tokens);

                            }
                        } catch (KiteException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else if(order.transactionType.equals(Constants.TRANSACTION_TYPE_SELL)){
                        if(order.orderId != sellOrder.orderId){
                            if(order.status.equals(Constants.ORDER_COMPLETE)){
                                logger.info(instr.getTradingSymbol() + order.price + order.orderType + order.quantity);
                                tickerProvider.disconnect();
                            }else if(order.status.equals(Constants.ORDER_TRIGGER_PENDING)){
//                                todo: once sell order gets failed or rejected
                                logger.info("sell order updated "+ order.price);
                                sellPrice = Double.parseDouble(order.price);
                            }else if(order.status.equals(Constants.ORDER_REJECTED)){
                                logger.info("order rejected" + order.statusMessage);
                            }
                        }

                    }
                }
            }
        });

        tickerProvider.setOnDisconnectedListener(() -> {
            try {
                logger.info(kiteSdk.getMargins().toString());
                tickerProvider.unsubscribe(tokens);
            } catch (KiteException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        tickerProvider.setOnTickerArrivalListener(new OnTicks() {
            @Override
            public void onTicks(ArrayList<Tick> ticks) {
                NumberFormat formatter = new DecimalFormat();
                logger.info("ticks size "+ticks.size());
                if(ticks.size() > 0) {
                    for (Tick tick:ticks) {
                        if(tick.getInstrumentToken()== instr.getInstrument_token()){
                            logger.info("Tick " + tick.getLastTradedPrice());
                            logger.info("Current sell price " +sellPrice);
                            if(tick.getLastTradedPrice()>sellPrice*1.05){
                                try {
                                    logger.info("trail by 4 percent on every 1 percent movement");
                                    double price = tick.getLastTradedPrice() *0.96;
                                    double trigger = tick.getLastTradedPrice() *0.97;
                                    price =(int)(price/instr.tick_size)*instr.tick_size;
                                    trigger =(int)(trigger/instr.tick_size)*instr.tick_size;
                                    OrderParams orderP = createSellOrder(instr, price, trigger, qty);
                                    sellPrice = price;
                                    sellOrder =kiteSdk.modifyOrder(sellOrder.orderId,orderP,Constants.VARIETY_REGULAR);
                                } catch (KiteException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            }
        });
        tickerProvider.connect();
    }


}
