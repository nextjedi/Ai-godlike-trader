package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.Call;
import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.util.KiteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeExecutorService {
    @Autowired
    private KiteUtil kiteUtil;
    @Autowired
    private CallService callService;
    private List<Call> calls =new ArrayList<>();
    public void init(){
//        todo schedule
        kiteUtil.establishTickConnection();
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
