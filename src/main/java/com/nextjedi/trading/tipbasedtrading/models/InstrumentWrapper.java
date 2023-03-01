package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class InstrumentWrapper {
    @Id
    public long instrument_token;
    public long exchange_token;
    public String tradingSymbol;
    public String name;
    public double last_price;
    public double tick_size;
    public String instrumentType;
    public String segment;
    public String exchange;
    public int strike;
    public int lot_size;
    @Temporal(TemporalType.DATE)
    public Date expiry;

    public InstrumentWrapper() {
    }

    public InstrumentWrapper(com.zerodhatech.models.Instrument instrument) {
        instrument_token = instrument.getInstrument_token();
        exchange_token = instrument.getExchange_token();
        tradingSymbol = instrument.getTradingsymbol();
        name = instrument.getName();
        last_price = instrument.getLast_price();
        tick_size = instrument.getTick_size();
        instrumentType = instrument.getInstrument_type();
        segment = instrument.getSegment();
        exchange = instrument.getExchange();
        strike = Integer.parseInt(instrument.getStrike());
        lot_size = instrument.getLot_size();
        expiry = instrument.getExpiry();
    }
    public long getInstrument_token() {
            return this.instrument_token;
}

    public void setInstrument_token(long instrument_token) {
        this.instrument_token = instrument_token;
    }

    public long getExchange_token() {
        return this.exchange_token;
    }

    public void setExchange_token(long exchange_token) {
        this.exchange_token = exchange_token;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTradingSymbol() {
        return this.tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public double getLast_price() {
        return this.last_price;
    }

    public void setLast_price(double last_price) {
        this.last_price = last_price;
    }

    public double getTick_size() {
        return this.tick_size;
    }

    public void setTick_size(double tick_size) {
        this.tick_size = tick_size;
    }

    public Date getExpiry() {
        return this.expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public String getInstrumentType() {
        return this.instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getSegment() {
        return this.segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getExchange() {
        return this.exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public int getStrike() {
        return this.strike;
    }

    public void setStrike(int strike) {
        this.strike = strike;
    }

    public int getLot_size() {
        return this.lot_size;
    }

    public void setLot_size(int lot_size) {
        this.lot_size = lot_size;
    }
}
