package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnOrderUpdate;
import com.zerodhatech.ticker.OnTicks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;

@Service
public class TipBasedTradingService {
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
            return;
        }
//        todo type of order
        String orderType;
        if(quote.lastPrice < tipModel.getPrice())
//            todo: putting a buy order at higher price explore in detail
            orderType = Constants.ORDER_TYPE_LIMIT;
        else if(quote.lastPrice< tipModel.getPrice() * 1.07){
            orderType = Constants.ORDER_TYPE_MARKET;
        }else {
            return;
        }



        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** Subscribe ticks for token.
                 * By default, all tokens are subscribed for modeQuote.
                 * */

                OrderParams orderParams = createBuyOrder(instr, tipModel.getPrice(),orderType);
                try {
                    buyOrder = kiteSdk.placeOrder(orderParams, Constants.VARIETY_REGULAR);
                } catch (KiteException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        tickerProvider.setOnOrderUpdateListener(new OnOrderUpdate() {
            @Override
            public void onOrderUpdate(Order order) {
                System.out.println("order update "+order.orderId);
                if(order.tag.equals(tag)){
                    if(order.transactionType.equals(Constants.TRANSACTION_TYPE_BUY)){
                        try {
                            if(!order.orderId.equals(buyOrder.orderId)){
                                return;
                            }
//                            place sell order and subscribe
                            if(order.status.equals(Constants.ORDER_COMPLETE)){
                                System.out.println(instr.getTradingSymbol() + order.price + order.orderType + order.quantity);
                                double price = Double.parseDouble(order.averagePrice)*0.80;
                                double trigger = Double.parseDouble(order.averagePrice)*0.85;
//                                todo: move to helper method
                                price =((int)(price/instr.tick_size))*instr.tick_size;
                                trigger =((int)(trigger/instr.tick_size))*instr.tick_size;
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
                                System.out.println(instr.getTradingSymbol() + order.price + order.orderType + order.quantity);
                                tickerProvider.disconnect();
                            }else if(order.status.equals(Constants.ORDER_TRIGGER_PENDING)){
                                sellPrice = Double.parseDouble(order.price);
                            }
                        }

                    }
                }
            }
        });

        tickerProvider.setOnDisconnectedListener(() -> {
            try {
                System.out.println(kiteSdk.getMargins().toString());
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
                System.out.println("ticks size "+ticks.size());
                if(ticks.size() > 0) {
                    for (Tick tick:ticks) {
                        if(tick.getInstrumentToken()== instr.getInstrument_token()){
//                            double trigger = Double.parseDouble(sellOrder.triggerPrice);

                            if(tick.getLastTradedPrice()>sellPrice*1.05){
                                try {
                                    double price = sellPrice;
                                    double trigger = sellPrice*1.01;
                                    price =(int)(price/instr.tick_size)*instr.tick_size;
                                    trigger =(int)(trigger/instr.tick_size)*instr.tick_size;
                                    sellPrice = tick.getLastTradedPrice();
                                    OrderParams orderP = createSellOrder(instr, price, trigger, qty);
                                    sellOrder =kiteSdk.modifyOrder(sellOrder.orderId,orderP,Constants.VARIETY_REGULAR);
                                } catch (KiteException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    System.out.println("last price "+ticks.get(0).getLastTradedPrice());
                    System.out.println("open interest "+formatter.format(ticks.get(0).getOi()));
                    System.out.println("day high OI "+formatter.format(ticks.get(0).getOpenInterestDayHigh()));
                    System.out.println("day low OI "+formatter.format(ticks.get(0).getOpenInterestDayLow()));
                    System.out.println("change "+formatter.format(ticks.get(0).getChange()));
                    System.out.println("tick timestamp "+ticks.get(0).getTickTimestamp());
                    System.out.println("tick timestamp date "+ticks.get(0).getTickTimestamp());
                    System.out.println("last traded time "+ticks.get(0).getLastTradedTime());
                    System.out.println(ticks.get(0).getMarketDepth().get("buy").size());
                }
            }
        });
        tickerProvider.connect();
    }


}
