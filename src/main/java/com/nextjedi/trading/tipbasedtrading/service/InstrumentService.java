package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.InstrumentRepository;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstrumentService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    InstrumentRepository instrumentRepository;
    public KiteConnect connectToKite(){
        String apikey = "2himf7a1ff5edpjy";
        String apiSecret = "87mebxtvu3226igmjnkjfjfcrgiphfxb";
        TokenAccess tokenAccess=tokenService.getToken();
        KiteConnect kiteSdk = new KiteConnect(apikey);
        kiteSdk.setAccessToken(tokenAccess.getAccesstoken());
        kiteSdk.setPublicToken(tokenAccess.getPublicToken());
        return kiteSdk;
    }

    public void insertInstruments(){
        KiteConnect kiteSdk = connectToKite();
        try {
            ArrayList<Instrument> instruments = (ArrayList<Instrument>) kiteSdk.getInstruments();
            List<InstrumentWrapper> instrumentWrappers
                =instruments.stream()
                    .filter(instrument -> instrument.getName() !=null && (instrument.getName().equals("FINNIFTY") ||instrument.getName().equals("BANKNIFTY")))
                    .map(instrument -> new InstrumentWrapper(instrument)).collect(Collectors.toList());
            instrumentRepository.deleteAll();
            instrumentRepository.saveAll(instrumentWrappers);

        } catch (KiteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InstrumentWrapper findInstrument(InstrumentQuery instrumentQuery){
        return instrumentRepository.findByStrikeAndNameAndInstrumentTypeAndSegmentAndExpiry(
                instrumentQuery.getStrike(), instrumentQuery.getName(), instrumentQuery.getInstrumentType(),"NFO-OPT", instrumentQuery.getExpiry());
    }
}
