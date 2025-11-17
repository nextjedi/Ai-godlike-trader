package com.nextjedi.trading.tipbasedtrading.broker.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Account balance and margin information
 */
@Data
@Builder
public class Balance {
    private Double available;           // Available cash
    private Double utilized;            // Utilized margin
    private Double total;               // Total balance
    private Double collateral;          // Collateral value
    private Double openingBalance;      // Opening balance for the day
    private Double live;                // Live margin (available for trading)
    private Double payin;               // Amount paid in
    private Double payout;              // Amount paid out
    private Double unrealizedPnl;       // Unrealized P&L
    private Double realizedPnl;         // Realized P&L
}
