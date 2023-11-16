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
//    https://stackoverflow.com/questions/28304384/how-to-correctly-implement-a-spring-websocket-java-client
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



}
