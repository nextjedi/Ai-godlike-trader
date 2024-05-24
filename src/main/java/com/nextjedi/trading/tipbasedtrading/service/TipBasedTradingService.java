package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TipModelRequest;
import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.service.connecttoexchange.ZerodhaConnectService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.LTPQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class TipBasedTradingService {
    private final InstrumentService instrumentService;
    private final ZerodhaConnectService zerodhaConnectService;
    private final TradeModelService tradeModelService;
    private final KiteService kiteService;

    public TipBasedTradingService(InstrumentService instrumentService, ZerodhaConnectService zerodhaConnectService, TradeModelService tradeModelService, KiteService kiteService) {
        this.instrumentService = instrumentService;
        this.zerodhaConnectService = zerodhaConnectService;
        this.tradeModelService = tradeModelService;
        this.kiteService = kiteService;
    }

    public void trade(TipModelRequest tipModelRequest) throws IOException, KiteException {
        log.info("instrument {}", tipModelRequest.getInstrument().toString());
        KiteConnect kiteSdk =zerodhaConnectService.getKiteConnect();
        InstrumentWrapper instr = instrumentService.findInstrumentWithEarliestExpiryFromToday(tipModelRequest.getInstrument());
        log.info("instrument found");
        Map<String, LTPQuote> quoteMap = kiteSdk.getLTP(new String[]{String.valueOf(instr.getInstrumentToken())});
        var quote = quoteMap.get(String.valueOf(instr.getInstrumentToken()));
        TradeModel tradeModel;
        double lastPrice = 0;
        if(Objects.nonNull(quote)){
            lastPrice = quote.lastPrice;
        }
        tradeModel = new TradeModel(instr,tipModelRequest,lastPrice);
        log.info("going to register new trade");
        if(tradeModelService.registerNewTrade(tradeModel)){
            log.info("going to subscribe to ticks for this instrument {}",instr);
            kiteService.subscribe(instr.getInstrumentToken());
        }
    }


}
