package com.nextjedi.trading.tipbasedtrading.models;

import java.sql.Date;

public class InstrumentQuery {
    private int Strike;
    private String name;
    private String instrumentType;
    private Date expiry;

    public int getStrike() {
        return Strike;
    }

    public void setStrike(int strike) {
        Strike = strike;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }
}
