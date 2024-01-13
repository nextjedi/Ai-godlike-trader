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
    public boolean registerNewTrade(TradeModel tradeModel){
        log.info("registerNewTrade service method");
        tradeModelRepository.save(tradeModel);
        return true;
    }
    public List<TradeModel> getAllActiveTrades(){
//        todo not in multiple statuses
        return tradeModelRepository.findTradeStatusNot(TradeStatus.COMPLETED);
    }
    public ArrayList<Long> getAllTokens(){
        var trades = getAllActiveTrades();
        var tokens = trades.stream().map(tradeModel -> tradeModel.getInstrument().getInstrumentToken()).distinct().toList();
        return new ArrayList<>(tokens);
    }
    public TradeModel getOrderByInstrumentToken(Long token){
        var res =tradeModelRepository.findTopByInstrument_InstrumentTokenAndTradeStatusNot(token,TradeStatus.COMPLETED);
        return res.orElseGet(null);
    }
    public TradeModel getOrderByInstrumentToken(Long token){
        var res =tradeModelRepository.findTopByInstrument_InstrumentTokenAndTradeStatusNot(token,TradeStatus.COMPLETED);
        return res.orElseGet(null);
    }
    public void updateTrade(TradeModel tradeModel){
        tradeModelRepository.save(tradeModel);
    }
}
