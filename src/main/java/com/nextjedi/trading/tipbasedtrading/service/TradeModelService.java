package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TradeModelRepository;
import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
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
        tradeModelRepository.save(tradeModel);
        return true;
    }
    public List<TradeModel> getAllTrades(){
        return tradeModelRepository.findAll();
    }
    public ArrayList<Long> getAllTokens(){
        var trades = tradeModelRepository.findAll();
        var tokens = trades.stream().map(tradeModel -> tradeModel.getInstrument().getInstrument_token()).collect(Collectors.toList());
        return new ArrayList<>(tokens);
    }
}
