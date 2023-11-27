package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TipModelRequest;
import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import com.nextjedi.trading.tipbasedtrading.service.connecttoexchange.ZerodhaConnectService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class TipBasedTradingService {
    @Autowired
    private InstrumentService instrumentService;
    @Autowired
    private ZerodhaConnectService zerodhaConnectService;
    @Autowired
    private TradeModelService tradeModelService;
    @Autowired
    private KiteService kiteService;
    String tag = "Ap2";
    public void trade(TipModelRequest tipModelRequest) throws IOException, KiteException {
        log.info("instrument" + tipModelRequest.getInstrument().toString() );
        KiteConnect kiteSdk =zerodhaConnectService.getKiteConnect();
        InstrumentWrapper instr = instrumentService.findInstrumentWithEarliestExpiry(tipModelRequest.getInstrument());
        log.info("instrument found");
        Map<String, LTPQuote> quoteMap = kiteSdk.getLTP(new String[]{String.valueOf(instr.getInstrument_token())});
        var quote = quoteMap.get(String.valueOf(instr.getInstrument_token()));
        var tradeModel = new TradeModel(instr,tipModelRequest.getPrice(),quote.lastPrice);
        if(tradeModelService.registerNewTrade(tradeModel)){
            kiteService.subscribe(instr.getInstrument_token());
        }
    }


}
