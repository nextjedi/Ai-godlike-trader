package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface InstrumentRepository extends JpaRepository<InstrumentWrapper,Long> {

    public InstrumentWrapper findByTradingSymbol(String tradingSymbol);
    public InstrumentWrapper findByStrikeAndNameAndInstrumentTypeAndSegmentAndExpiry(int strike, String name,String instrumentType,String Segment, Date expiry);
    public List<InstrumentWrapper> findByStrikeAndNameAndInstrumentTypeAndSegment(int strike, String name,String instrumentType, String Segment);
    public InstrumentWrapper findTopByStrikeAndNameAndInstrumentTypeAndSegment(
            int strike, String name, String instrumentType, String segment, Sort sort);
}
