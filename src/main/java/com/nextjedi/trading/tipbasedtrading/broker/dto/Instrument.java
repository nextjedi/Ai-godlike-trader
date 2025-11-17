package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Instrument information
 */
@Data
@Builder
public class Instrument {
    private Long instrumentToken;
    private String tradingSymbol;
    private String name;
    private String exchange;
    private String segment;            // EQ, FUT, OPT, etc.
    private String instrumentType;     // CE, PE, FUT, EQ, etc.
    private Double strike;             // Strike price for options
    private LocalDate expiry;          // Expiry date for F&O
    private Double tickSize;           // Minimum price movement
    private Integer lotSize;           // Lot size for F&O
    private String exchangeToken;
}
