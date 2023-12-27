package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class TradeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instrument_instrument_token")
    private InstrumentWrapper instrument;
    private double triggerPrice;
    private double stopLoss;
    private double target;
    private TradeType type;
//    entry means buy
    private double priceWhenTipIsReceived;
    private TradeStatus tradeStatus;
    private boolean isEngaged;

    public TradeModel(InstrumentWrapper instr, int price, double lastPrice) {
        instrument = instr;
        triggerPrice = price;
        priceWhenTipIsReceived = lastPrice;
        this.tradeStatus = TradeStatus.NEW;
    }
}
