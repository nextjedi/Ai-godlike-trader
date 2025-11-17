package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request object for placing/modifying orders
 */
@Data
@Builder
public class OrderRequest {
    private String exchange;              // NSE, NFO, BSE, etc.
    private String tradingSymbol;         // Trading symbol
    private Long instrumentToken;         // Instrument token (optional)
    private TransactionType transactionType;  // BUY, SELL
    private OrderType orderType;          // MARKET, LIMIT, SL, SL-M
    private Integer quantity;             // Number of shares/lots
    private Double price;                 // Price for LIMIT orders
    private Double triggerPrice;          // Trigger price for SL/SL-M orders
    private Product product;              // MIS, NRML, CNC
    private Validity validity;            // DAY, IOC
    private String tag;                   // Optional tag for order

    public enum TransactionType {
        BUY, SELL
    }

    public enum OrderType {
        MARKET,    // Market order
        LIMIT,     // Limit order
        SL,        // Stop-loss limit order
        SL_M       // Stop-loss market order
    }

    public enum Product {
        MIS,       // Intraday
        NRML,      // Normal (carry forward)
        CNC        // Cash and carry
    }

    public enum Validity {
        DAY,       // Valid for the day
        IOC        // Immediate or cancel
    }
}
