package com.nextjedi.trading.tipbasedtrading.models;

import lombok.Data;

@Data
public class TipModelRequest {
    private InstrumentQuery instrument;
    private int price;
    private int stopLoss;
    private int target;
    private TradeType type;
}
