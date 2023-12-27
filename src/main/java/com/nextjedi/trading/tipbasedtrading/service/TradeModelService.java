package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TradeModelRepository;
import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TradeModelService {
    @Autowired
    private TradeModelRepository tradeModelRepository;
    private boolean dbFetchFlag = true;
    private List<TradeModel> tradeCalls = new ArrayList<>();
    public boolean registerNewTrade(TradeModel tradeModel){
        log.info("registerNewTrade service method");
        tradeModelRepository.save(tradeModel);
        dbFetchFlag = true;
        return true;
    }
    public List<TradeModel> getAllActiveTrades(){
//        todo correct the query
        if(dbFetchFlag){
           tradeCalls = tradeModelRepository.findAll();
           dbFetchFlag = false;
        }
        return tradeCalls.stream().filter(tradeModel -> !tradeModel.getTradeStatus().equals(TradeStatus.COMPLETED)).collect(Collectors.toList());
    }
    public ArrayList<Long> getAllTokens(){
        var trades = getAllActiveTrades();
        var tokens = trades.stream().map(tradeModel -> tradeModel.getInstrument().getInstrument_token()).distinct().collect(Collectors.toList());
        return new ArrayList<>(tokens);
    }
    public TradeModel getOrderByInstrumentToken(Long token){
        var trades = getAllActiveTrades();
//        todo implement
        return trades.get(0);
    }
}
