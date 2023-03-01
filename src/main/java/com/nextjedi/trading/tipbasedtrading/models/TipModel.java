package com.nextjedi.trading.tipbasedtrading.models;

public class TipModel {
    private InstrumentQuery instrument;
    private int price;

    public InstrumentQuery getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentQuery instrument) {
        this.instrument = instrument;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
