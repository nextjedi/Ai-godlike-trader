package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Strategy allocation - defines how much capital is allocated to each trading strategy
 */
@Entity
@Table(name = "strategy_allocations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /**
     * Strategy identifier (e.g., "tip-based-trading", "momentum-strategy")
     */
    @Column(nullable = false)
    private String strategyId;

    /**
     * Strategy name for display
     */
    @Column(nullable = false)
    private String strategyName;

    /**
     * Allocation type: PERCENTAGE or FIXED_AMOUNT
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationType allocationType;

    /**
     * Allocation value (percentage or fixed amount based on type)
     */
    @Column(nullable = false)
    private Double allocationValue;

    /**
     * Actual allocated amount in rupees
     */
    @Column(nullable = false)
    private Double allocatedAmount;

    /**
     * Maximum position size for a single trade in this strategy
     */
    @Column(nullable = false)
    private Double maxPositionSize;

    /**
     * Current exposure (capital utilized in open positions)
     */
    @Column(nullable = false)
    private Double currentExposure;

    /**
     * Realized P&L for this strategy
     */
    @Column(nullable = false)
    private Double realizedPnl;

    /**
     * Unrealized P&L for this strategy
     */
    @Column(nullable = false)
    private Double unrealizedPnl;

    /**
     * Total number of trades executed by this strategy
     */
    @Column(nullable = false)
    private Integer totalTrades;

    /**
     * Number of winning trades
     */
    @Column(nullable = false)
    private Integer winningTrades;

    /**
     * Number of losing trades
     */
    @Column(nullable = false)
    private Integer losingTrades;

    /**
     * Is this strategy allocation active?
     */
    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public enum AllocationType {
        PERCENTAGE,    // Allocate by percentage of total portfolio capital
        FIXED_AMOUNT   // Allocate fixed amount
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isActive == null) {
            isActive = true;
        }
        if (currentExposure == null) {
            currentExposure = 0.0;
        }
        if (realizedPnl == null) {
            realizedPnl = 0.0;
        }
        if (unrealizedPnl == null) {
            unrealizedPnl = 0.0;
        }
        if (totalTrades == null) {
            totalTrades = 0;
        }
        if (winningTrades == null) {
            winningTrades = 0;
        }
        if (losingTrades == null) {
            losingTrades = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Calculate allocated amount based on portfolio total capital
     */
    public void calculateAllocatedAmount(Double portfolioTotalCapital) {
        if (allocationType == AllocationType.PERCENTAGE) {
            allocatedAmount = (allocationValue / 100.0) * portfolioTotalCapital;
        } else {
            allocatedAmount = allocationValue;
        }
    }

    /**
     * Reserve capital for a new position
     */
    public void reserveCapital(Double amount) {
        if (amount > (allocatedAmount - currentExposure)) {
            throw new IllegalArgumentException(
                    String.format("Insufficient allocated capital for strategy %s. Available: %.2f, Requested: %.2f",
                            strategyName, allocatedAmount - currentExposure, amount));
        }
        currentExposure += amount;
    }

    /**
     * Release capital from a closed position
     */
    public void releaseCapital(Double amount, Double pnl) {
        currentExposure -= amount;
        realizedPnl += pnl;

        // Update trade counts
        totalTrades++;
        if (pnl > 0) {
            winningTrades++;
        } else if (pnl < 0) {
            losingTrades++;
        }
    }

    /**
     * Update unrealized P&L
     */
    public void updateUnrealizedPnl(Double newUnrealizedPnl) {
        this.unrealizedPnl = newUnrealizedPnl;
    }

    /**
     * Check if strategy can take a new position
     */
    public boolean canTakePosition(Double amount) {
        return amount <= maxPositionSize && (currentExposure + amount) <= allocatedAmount;
    }

    /**
     * Get available capital
     */
    public Double getAvailableCapital() {
        return allocatedAmount - currentExposure;
    }

    /**
     * Get win rate percentage
     */
    public Double getWinRate() {
        if (totalTrades == 0) {
            return 0.0;
        }
        return (winningTrades.doubleValue() / totalTrades.doubleValue()) * 100.0;
    }

    /**
     * Get utilization percentage
     */
    public Double getUtilizationPercent() {
        return (currentExposure / allocatedAmount) * 100.0;
    }

    /**
     * Get total P&L
     */
    public Double getTotalPnl() {
        return realizedPnl + unrealizedPnl;
    }

    /**
     * Get ROI percentage
     */
    public Double getRoi() {
        return (getTotalPnl() / allocatedAmount) * 100.0;
    }
}
