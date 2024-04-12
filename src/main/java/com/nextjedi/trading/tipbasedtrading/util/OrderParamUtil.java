package com.nextjedi.trading.tipbasedtrading.util;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TradeType;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.OrderParams;

public class OrderParamUtil {
    private OrderParamUtil(){

    }
    public static OrderParams createBuyOrder(InstrumentWrapper instrumentWrapper, double price, double balance, String orderType, String tag , TradeType type){

        int lotCount = (int) (balance /(price*instrumentWrapper.getLot_size()));
        if(lotCount == 0){
            return null;
        }
        OrderParams orderParams = new OrderParams();
        orderParams.quantity = instrumentWrapper.getLot_size()*lotCount;
        orderParams.orderType = orderType;
        orderParams.tradingsymbol = instrumentWrapper.getTradingSymbol();

        orderParams.product = getOrderType(type);
        orderParams.exchange = Constants.EXCHANGE_NFO;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = price;
        orderParams.tag = tag;  //tag is optional, and it cannot be more than 8 characters and only alphanumeric is allowed
        return orderParams;
    }

    public static OrderParams createSellOrder(InstrumentWrapper instrumentWrapper,double price,double trigger, int quantity, String tag,TradeType type){
        OrderParams orderParams = new OrderParams();
        orderParams.quantity = quantity;
        orderParams.orderType = Constants.ORDER_TYPE_SL;
        orderParams.tradingsymbol = instrumentWrapper.getTradingSymbol();
        orderParams.product = getOrderType(type);
        orderParams.exchange = Constants.EXCHANGE_NFO;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = price;
        orderParams.triggerPrice = trigger;
        orderParams.tag = tag;  //tag is optional, and it cannot be more than 8 characters and only alphanumeric is allowed
        return orderParams;
    }
    private static String getOrderType(TradeType type){
        return switch (type) {
            case BTST -> Constants.PRODUCT_NRML;
            case DAY -> Constants.PRODUCT_MIS;
        };

    }
}
