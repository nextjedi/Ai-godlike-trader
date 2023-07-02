package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.component.AzureSecrets;
import com.nextjedi.trading.tipbasedtrading.controller.TokenController;
import com.nextjedi.trading.tipbasedtrading.dao.InstrumentRepository;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstrumentService {

    Logger logger = LoggerFactory.getLogger(InstrumentService.class);
    @Autowired
    private TokenService tokenService;

    @Autowired
    private AzureSecrets azureSecrets;
    @Autowired
    InstrumentRepository instrumentRepository;
    public KiteConnect connectToKite(){
        String apikey = azureSecrets.getSecret("API-KEY");

        String apiSecret = azureSecrets.getSecret("API-SECRET");
        TokenAccess tokenAccess=tokenService.getToken();
        KiteConnect kiteSdk = new KiteConnect(apikey);
        kiteSdk.setAccessToken(tokenAccess.getAccesstoken());
        kiteSdk.setPublicToken(tokenAccess.getPublicToken());
        return kiteSdk;
    }

    public boolean insertInstruments(){
        logger.info("Updating instruments");
        KiteConnect kiteSdk = connectToKite();
        try {
            ArrayList<Instrument> instruments = (ArrayList<Instrument>) kiteSdk.getInstruments();
            List<InstrumentWrapper> instrumentWrappers
                =instruments.stream()
                    .filter(instrument -> instrument.getName() !=null && (instrument.getName().equals("FINNIFTY") ||instrument.getName().equals("BANKNIFTY")))
                    .map(instrument -> new InstrumentWrapper(instrument)).collect(Collectors.toList());
            logger.info("Instruments fetched" + instrumentWrappers.size());
            instrumentRepository.deleteAll();
            instrumentRepository.saveAll(instrumentWrappers);
            logger.info("Instruments updated");
            return true;

        } catch (KiteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InstrumentWrapper findInstrumentWithEarliestExpiry(InstrumentQuery instrumentQuery){
        List<InstrumentWrapper> instruments = instrumentRepository.findByStrikeAndNameAndInstrumentTypeAndSegment(
                instrumentQuery.getStrike(), instrumentQuery.getName(), instrumentQuery.getInstrumentType(), "NFO-OPT");
        logger.info("number of instrument "+instruments.size());
        Collections.sort(instruments, Comparator.comparing(InstrumentWrapper::getExpiry));
        return instruments.get(0);
    }
}
