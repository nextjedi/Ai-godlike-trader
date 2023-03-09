package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface InstrumentRepository extends JpaRepository<InstrumentWrapper,Long> {

    public InstrumentWrapper findByTradingSymbol(String tradingsymbol);
    public InstrumentWrapper findByStrikeAndNameAndInstrumentTypeAndSegmentAndExpiry(int Strike, String name,String instrumentType,String Segment, Date expiry);
    public List<InstrumentWrapper> findByStrikeAndNameAndInstrumentTypeAndSegment(int Strike, String name,String instrumentType, String Segment);
}
