package com.nextjedi.trading.tipbasedtrading.models;

import com.zerodhatech.models.Order;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@Entity
public class TradeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instrument_instrumentToken")
    private InstrumentWrapper instrument;
    private double triggerPrice;
    private double stopLoss;
    private double target;
    private TradeType type;
//    entry means buy
    private double priceWhenTipIsReceived;
    private TradeStatus tradeStatus;
    private String tag;
    @OneToOne(cascade = CascadeType.ALL)
    private OrderDetail entryOrder;
    @OneToOne(cascade = CascadeType.ALL)
    private OrderDetail exitOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    public TradeModel(InstrumentWrapper instr, TipModelRequest tipModelRequest, double lastPrice) {
        instrument = instr;
        triggerPrice = tipModelRequest.getPrice();
        target = tipModelRequest.getTarget();
        stopLoss = tipModelRequest.getStopLoss();
        priceWhenTipIsReceived = lastPrice;
        this.tradeStatus = TradeStatus.NEW;
    }
}
