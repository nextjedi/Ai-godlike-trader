package com.nextjedi.trading.tipbasedtrading.models;

import com.zerodhatech.models.Instrument;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class InstrumentWrapper {
    @Id
    public long instrument_token;
    public long exchange_token;
    public String tradingsymbol;
    public String name;
    public double last_price;
    public double tick_size;
    public String instrument_type;
    public String segment;
    public String exchange;
    public String strike;
    public int lot_size;
    public Date expiry;

    public InstrumentWrapper() {
    }

    public InstrumentWrapper(com.zerodhatech.models.Instrument instrument) {
        instrument_token = instrument.getInstrument_token();
        exchange_token = instrument.getExchange_token();
        tradingsymbol = instrument.getTradingsymbol();
        name = instrument.getName();
        last_price = instrument.getLast_price();
        tick_size = instrument.getTick_size();
        instrument_type = instrument.getInstrument_type();
        segment = instrument.getSegment();
        exchange = instrument.getExchange();
        strike = instrument.getStrike();
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

    public String getTradingsymbol() {
        return this.tradingsymbol;
    }

    public void setTradingsymbol(String tradingsymbol) {
        this.tradingsymbol = tradingsymbol;
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

    public String getInstrument_type() {
        return this.instrument_type;
    }

    public void setInstrument_type(String instrument_type) {
        this.instrument_type = instrument_type;
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

    public String getStrike() {
        return this.strike;
    }

    public void setStrike(String strike) {
        this.strike = strike;
    }

    public int getLot_size() {
        return this.lot_size;
    }

    public void setLot_size(int lot_size) {
        this.lot_size = lot_size;
    }
}
