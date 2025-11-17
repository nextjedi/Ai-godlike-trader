package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Market tick data
 */
@Data
@Builder
public class Tick {
    private Long instrumentToken;
    private String tradingSymbol;
    private Double lastPrice;
    private Integer lastQuantity;
    private Integer volume;
    private Integer averageTradePrice;
    private Integer buyQuantity;
    private Integer sellQuantity;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Double bidPrice;
    private Double askPrice;
    private Integer bidQuantity;
    private Integer askQuantity;
    private Integer openInterest;
    private Integer oiDayHigh;
    private Integer oiDayLow;
    private Instant timestamp;
    private Instant exchangeTimestamp;
}
