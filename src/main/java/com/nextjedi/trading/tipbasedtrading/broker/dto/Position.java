package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Position information
 */
@Data
@Builder
public class Position {
    private String exchange;
    private String tradingSymbol;
    private Long instrumentToken;
    private OrderRequest.Product product;
    private Integer quantity;              // Net quantity (positive for long, negative for short)
    private Integer buyQuantity;           // Total buy quantity
    private Integer sellQuantity;          // Total sell quantity
    private Double averagePrice;           // Average price
    private Double buyPrice;               // Average buy price
    private Double sellPrice;              // Average sell price
    private Double lastPrice;              // Last traded price
    private Double pnl;                    // Realized P&L
    private Double unrealizedPnl;          // Unrealized P&L
    private Double value;                  // Current value of position
    private Double buyValue;               // Total buy value
    private Double sellValue;              // Total sell value
}
