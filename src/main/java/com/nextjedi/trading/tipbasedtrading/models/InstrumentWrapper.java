package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Data
@ToString
@NoArgsConstructor
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
}
