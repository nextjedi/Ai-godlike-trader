package com.nextjedi.trading.tipbasedtrading.models;

import lombok.Data;

@Data
public class TipModelRequest {
    private InstrumentQuery instrument;
    private int price;
    private TradeType type;
}
