package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.exception.InstrumentNotFoundException;
import com.nextjedi.trading.tipbasedtrading.dao.InstrumentRepository;
import com.nextjedi.trading.tipbasedtrading.models.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nextjedi.trading.tipbasedtrading.models.Constants.*;

@Service
@Slf4j
public class InstrumentService {
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
        log.info("Updating instruments");
        KiteConnect kiteSdk = connectToKite();
        try {
            List<Instrument> instruments = kiteSdk.getInstruments();
            List<InstrumentWrapper> instrumentWrappers
                =instruments.stream()
                    .filter(instrument -> instrument.getName() !=null && (instrument.getName().equals("FINNIFTY") ||instrument.getName().equals("BANKNIFTY")))
                    .map(InstrumentWrapper::new).toList();
            log.info("Instruments fetched" + instrumentWrappers.size());
            instrumentRepository.deleteAll();
            instrumentRepository.saveAll(instrumentWrappers);
            log.info("Instruments updated");
            return true;

        } catch (KiteException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InstrumentWrapper findInstrumentWithEarliestExpiry(InstrumentQuery instrumentQuery){
        log.info("inside find instrument for the call");
        try {
            var instrument = instrumentRepository.findTopByStrikeAndNameAndInstrumentTypeAndSegment(
                    instrumentQuery.getStrike(), instrumentQuery.getName(), instrumentQuery.getInstrumentType(), OPTION_SEGMENT, Sort.by(Sort.Direction.DESC,EXPIRY));
            if(Objects.nonNull(instrument)){
                log.info("instrument found", instrument.getName());
                return instrument;
            }else {
                throw new InstrumentNotFoundException("Instrument is null");
            }
        }catch (Exception e){
            log.error("Instrument not available", e.getMessage());
            log.error("Query", instrumentQuery.toString());
            throw new InstrumentNotFoundException(e.getMessage());
        }
    }
}
