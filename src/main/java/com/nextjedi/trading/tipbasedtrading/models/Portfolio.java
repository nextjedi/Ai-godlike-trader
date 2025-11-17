package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Portfolio aggregate root - manages overall capital allocation
 */
@Entity
@Table(name = "portfolios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String userId;

    /**
     * Total capital in the portfolio
     */
    @Column(nullable = false)
    private Double totalCapital;

    /**
     * Available capital (not allocated to any strategy)
     */
    @Column(nullable = false)
    private Double availableCapital;

    /**
     * Capital allocated to strategies (sum of all strategy allocations)
     */
    @Column(nullable = false)
    private Double allocatedCapital;

    /**
     * Capital currently utilized in open positions
     */
    @Column(nullable = false)
    private Double utilizedCapital;

    /**
     * Realized P&L
     */
    @Column(nullable = false)
    private Double realizedPnl;

    /**
     * Unrealized P&L from open positions
     */
    @Column(nullable = false)
    private Double unrealizedPnl;

    /**
     * Maximum drawdown percentage allowed
     */
    @Column(nullable = false)
    private Double maxDrawdownPercent;

    /**
     * Maximum total exposure as percentage of capital
     */
    @Column(nullable = false)
    private Double maxExposurePercent;

    /**
     * Is this portfolio active?
     */
    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StrategyAllocation> strategyAllocations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isActive == null) {
            isActive = true;
        }
        if (availableCapital == null) {
            availableCapital = totalCapital;
        }
        if (allocatedCapital == null) {
            allocatedCapital = 0.0;
        }
        if (utilizedCapital == null) {
            utilizedCapital = 0.0;
        }
        if (realizedPnl == null) {
            realizedPnl = 0.0;
        }
        if (unrealizedPnl == null) {
            unrealizedPnl = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Allocate capital to a strategy
     */
    public void allocateToStrategy(StrategyAllocation allocation) {
        if (allocation.getAllocatedAmount() > availableCapital) {
            throw new IllegalArgumentException(
                    String.format("Insufficient available capital. Available: %.2f, Requested: %.2f",
                            availableCapital, allocation.getAllocatedAmount()));
        }

        strategyAllocations.add(allocation);
        allocation.setPortfolio(this);

        availableCapital -= allocation.getAllocatedAmount();
        allocatedCapital += allocation.getAllocatedAmount();
    }

    /**
     * Deallocate capital from a strategy
     */
    public void deallocateFromStrategy(StrategyAllocation allocation) {
        strategyAllocations.remove(allocation);

        availableCapital += allocation.getAllocatedAmount();
        allocatedCapital -= allocation.getAllocatedAmount();
    }

    /**
     * Reserve capital when entering a trade
     */
    public void reserveCapital(Double amount) {
        if (amount > (allocatedCapital - utilizedCapital)) {
            throw new IllegalArgumentException(
                    String.format("Insufficient allocated capital. Available: %.2f, Requested: %.2f",
                            allocatedCapital - utilizedCapital, amount));
        }
        utilizedCapital += amount;
    }

    /**
     * Release capital when exiting a trade
     */
    public void releaseCapital(Double amount, Double pnl) {
        utilizedCapital -= amount;
        realizedPnl += pnl;

        // Update total capital based on P&L
        totalCapital += pnl;
        availableCapital += pnl;
    }

    /**
     * Update unrealized P&L
     */
    public void updateUnrealizedPnl(Double newUnrealizedPnl) {
        this.unrealizedPnl = newUnrealizedPnl;
    }

    /**
     * Check if portfolio can take a new position
     */
    public boolean canTakePosition(Double amount) {
        Double currentExposure = (utilizedCapital + amount) / totalCapital * 100;
        return currentExposure <= maxExposurePercent;
    }

    /**
     * Get current drawdown percentage
     */
    public Double getCurrentDrawdown() {
        Double peakCapital = totalCapital + Math.abs(realizedPnl); // Approximate peak
        return ((peakCapital - (totalCapital + unrealizedPnl)) / peakCapital) * 100;
    }

    /**
     * Check if max drawdown is breached
     */
    public boolean isMaxDrawdownBreached() {
        return getCurrentDrawdown() > maxDrawdownPercent;
    }

    /**
     * Get total portfolio value (capital + unrealized P&L)
     */
    public Double getTotalValue() {
        return totalCapital + unrealizedPnl;
    }

    /**
     * Get utilization percentage
     */
    public Double getUtilizationPercent() {
        return (utilizedCapital / allocatedCapital) * 100;
    }
}
