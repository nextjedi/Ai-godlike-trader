package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TipModel;
import com.nextjedi.trading.tipbasedtrading.util.Helper;
import com.nextjedi.trading.tipbasedtrading.util.OrderParamUtil;
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

@Service
@Slf4j
public class TipBasedTradingService {
    @Autowired
    private InstrumentService instrumentService;
    @Autowired
    private KiteService kiteService;
    String tag = "Ap2";
    public void trade(TipModel tipModel) throws IOException, KiteException {
        log.info("instrument" +tipModel.getInstrument().toString() );
//        create a CallTrade object
//        send it to tradeExecutor service
        KiteConnect kiteSdk = kiteService.connectToKite();
        KiteTicker tickerProvider = new KiteTicker(kiteSdk.getAccessToken(), kiteSdk.getApiKey());
        InstrumentWrapper instr = instrumentService.findInstrumentWithEarliestExpiry(tipModel.getInstrument());
        ArrayList<Long> tokens = new ArrayList<>();
        tokens.add((instr.getInstrument_token()));
        Map<String, LTPQuote> quoteMap = kiteSdk.getLTP(new String[]{String.valueOf(instr.getInstrument_token())});
        var quote = quoteMap.get(String.valueOf(instr.getInstrument_token()));
        float balance = Float.parseFloat(kiteSdk.getMargins(Constants.INSTRUMENTS_SEGMENTS_EQUITY).available.liveBalance);

//        todo: if balance is available
        if(balance < quote.lastPrice* instr.getLot_size()){
            log.warn("Balance not available");
            return;
        }
        log.info("Instruments found and balance available");
    }


}
