package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.InstrumentRepository;
import com.nextjedi.trading.tipbasedtrading.models.*;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.nextjedi.trading.tipbasedtrading.models.Constants.USER_ID;

@Service
public class InstrumentService {

    Logger logger = LoggerFactory.getLogger(InstrumentService.class);
    @Autowired
    private TokenService tokenService;

    @Autowired
    InstrumentRepository instrumentRepository;
    public KiteConnect connectToKite(){
        var secret =ApiSecret.apiKeys.get(USER_ID);
        TokenAccess tokenAccess=tokenService.getLatestTokenByUserId(USER_ID);
        KiteConnect kiteSdk = new KiteConnect(secret.getApiKey());
        kiteSdk.setAccessToken(tokenAccess.getAccessToken());
        kiteSdk.setPublicToken(tokenAccess.getPublicToken());
        return kiteSdk;
    }

    public boolean insertInstruments(){
        logger.info("Updating instruments");
        KiteConnect kiteSdk = connectToKite();
        try {
            List<Instrument> instruments = kiteSdk.getInstruments();
            List<InstrumentWrapper> instrumentWrappers
                =instruments.stream()
                    .filter(instrument -> EnumUtils.isValidEnumIgnoreCase(InstrumentNames.class,instrument.getName()))
                    .map(InstrumentWrapper::new).collect(Collectors.toList());
            logger.info("Instruments fetched", instrumentWrappers.size());
//            todo: call on need basis not everyday
            instrumentRepository.deleteAll();
            instrumentRepository.saveAll(instrumentWrappers);
            logger.info("Instruments updated");
            return true;

        } catch (KiteException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InstrumentWrapper findInstrumentWithEarliestExpiry(InstrumentQuery instrumentQuery){
        List<InstrumentWrapper> instruments = instrumentRepository.findByStrikeAndNameAndInstrumentTypeAndSegment(
                instrumentQuery.getStrike(), instrumentQuery.getName(), instrumentQuery.getInstrumentType(), "NFO-OPT");
        logger.info("number of instrument ", instruments.size());
        Collections.sort(instruments, Comparator.comparing(InstrumentWrapper::getExpiry));
        return instruments.get(0);
    }
}
