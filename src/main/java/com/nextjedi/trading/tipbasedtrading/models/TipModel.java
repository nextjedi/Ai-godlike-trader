package com.nextjedi.trading.tipbasedtrading.models;

public class TipModel {
    private String instrument;
    private int price;

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
