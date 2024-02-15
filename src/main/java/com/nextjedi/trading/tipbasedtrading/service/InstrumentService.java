package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.exception.InstrumentNotFoundException;
import com.nextjedi.trading.tipbasedtrading.dao.InstrumentRepository;
import com.nextjedi.trading.tipbasedtrading.service.connecttoexchange.ZerodhaConnectService;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.nextjedi.trading.tipbasedtrading.util.Constants.*;

@Service
@Slf4j
public class InstrumentService {
    @Autowired
    InstrumentRepository instrumentRepository;
    @Autowired
    ZerodhaConnectService zerodhaConnectService;

//        @Scheduled(cron = "0 45 8 * * MON-FRI")
        public boolean insertInstruments(){
        log.info("Updating instruments");
        KiteConnect kiteSdk = zerodhaConnectService.getKiteConnect();
        try {
            List<Instrument> instruments = kiteSdk.getInstruments();
            List<InstrumentWrapper> instrumentWrappers
                =instruments.stream()
                    .filter(instrument -> instrument.getName() !=null && (instrument.getName().equals("FINNIFTY") ||instrument.getName().equals("BANKNIFTY")))
                    .map(InstrumentWrapper::new).toList();
            log.info("Instruments fetched {}", instrumentWrappers.size());
            var instrumentsSaved =instrumentRepository.findAll();
            var instsWrapperList =instrumentWrappers.stream().filter(instrumentWrapper -> instrumentsSaved.stream().filter(instrumentWrapper1 -> instrumentWrapper.getInstrumentToken() == instrumentWrapper1.getInstrumentToken()).count() ==0).collect(Collectors.toList());
            instrumentRepository.saveAll(instsWrapperList);
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
                    instrumentQuery.getStrike(), instrumentQuery.getName(), instrumentQuery.getInstrumentType(), OPTION_SEGMENT, Sort.by(Sort.Direction.ASC,EXPIRY));
            if(Objects.nonNull(instrument)){
                log.info("instrument found "+ instrument.getName());
                return instrument;
            }else {
                throw new InstrumentNotFoundException("Instrument is null");
            }
        }catch (Exception e){
            log.error("Instrument not available"+ e.getMessage());
            log.error("need to add this instrument {}", instrumentQuery.getName());
            throw new InstrumentNotFoundException(e.getMessage());
        }
    }
    public InstrumentWrapper findInstrumentWithEarliestExpiryFromToday(InstrumentQuery instrumentQuery){
        log.info("inside find instrument for the call");
        try {
            var instruments = instrumentRepository.findByStrikeAndNameAndInstrumentTypeAndSegment(
                    instrumentQuery.getStrike(), instrumentQuery.getName(), instrumentQuery.getInstrumentType(), OPTION_SEGMENT);
            if(!CollectionUtils.isEmpty(instruments)){
                instruments =instruments.stream().filter(instrumentWrapper -> instrumentWrapper.getExpiry().after(Date.from(Instant.now().minus(1,ChronoUnit.DAYS)))).collect(Collectors.toList());
                instruments.sort(Comparator.comparing(InstrumentWrapper::getExpiry));

                log.info("instrument found "+ instruments.get(0).getName());
                return instruments.get(0);
            }else {
                throw new InstrumentNotFoundException("Instrument is null");
            }
        }catch (Exception e){
            log.error("Instrument not available"+ e.getMessage());
            log.error("Query"+ instrumentQuery.toString());
            throw new InstrumentNotFoundException(e.getMessage());
        }
    }
}
