package com.nextjedi.trading.tipbasedtrading.broker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * Request object for placing/modifying orders
 */
@Data
@Builder
public class OrderRequest {

    @NotBlank(message = "Exchange is required")
    private String exchange;              // NSE, NFO, BSE, etc.

    @NotBlank(message = "Trading symbol is required")
    private String tradingSymbol;         // Trading symbol

    private Long instrumentToken;         // Instrument token (optional)

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;  // BUY, SELL

    @NotNull(message = "Order type is required")
    private OrderType orderType;          // MARKET, LIMIT, SL, SL-M

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;             // Number of shares/lots

    @Positive(message = "Price must be positive")
    private Double price;                 // Price for LIMIT orders

    @Positive(message = "Trigger price must be positive")
    private Double triggerPrice;          // Trigger price for SL/SL-M orders

    @NotNull(message = "Product type is required")
    private Product product;              // MIS, NRML, CNC

    @NotNull(message = "Validity is required")
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
