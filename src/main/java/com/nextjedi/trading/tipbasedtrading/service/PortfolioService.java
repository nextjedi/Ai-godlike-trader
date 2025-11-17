package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.PortfolioRepository;
import com.nextjedi.trading.tipbasedtrading.dao.StrategyAllocationRepository;
import com.nextjedi.trading.tipbasedtrading.events.EventPublisher;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.FundsAllocatedEvent;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.PositionClosedEvent;
import com.nextjedi.trading.tipbasedtrading.events.portfolio.PositionOpenedEvent;
import com.nextjedi.trading.tipbasedtrading.models.Portfolio;
import com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing portfolios and capital allocation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final StrategyAllocationRepository strategyAllocationRepository;
    private final EventPublisher eventPublisher;

    /**
     * Create a new portfolio
     */
    @Transactional
    public Portfolio createPortfolio(Portfolio portfolio) {
        log.info("Creating portfolio: {} for user: {}", portfolio.getName(), portfolio.getUserId());

        // Validate
        if (portfolioRepository.findByName(portfolio.getName()).isPresent()) {
            throw new IllegalArgumentException("Portfolio with name already exists: " + portfolio.getName());
        }

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio created with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Get portfolio by ID
     */
    public Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));
    }

    /**
     * Get user's active portfolio
     */
    public Portfolio getUserActivePortfolio(String userId) {
        return portfolioRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active portfolio found for user: " + userId));
    }

    /**
     * Get all portfolios for a user
     */
    public List<Portfolio> getUserPortfolios(String userId) {
        return portfolioRepository.findByUserId(userId);
    }

    /**
     * Allocate funds to a strategy
     */
    @Transactional
    public StrategyAllocation allocateFunds(Long portfolioId, String strategyId, String strategyName,
                                           StrategyAllocation.AllocationType allocationType,
                                           Double allocationValue, Double maxPositionSize) {
        log.info("Allocating funds to strategy {} in portfolio {}", strategyId, portfolioId);

        Portfolio portfolio = getPortfolio(portfolioId);

        // Check if strategy already has allocation
        if (strategyAllocationRepository.findByPortfolioIdAndStrategyIdAndIsActiveTrue(portfolioId, strategyId).isPresent()) {
            throw new IllegalArgumentException("Strategy already has active allocation in this portfolio: " + strategyId);
        }

        // Create allocation
        StrategyAllocation allocation = StrategyAllocation.builder()
                .portfolio(portfolio)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .allocationType(allocationType)
                .allocationValue(allocationValue)
                .maxPositionSize(maxPositionSize)
                .build();

        // Calculate allocated amount
        allocation.calculateAllocatedAmount(portfolio.getTotalCapital());

        // Add to portfolio
        portfolio.allocateToStrategy(allocation);

        // Save
        StrategyAllocation saved = strategyAllocationRepository.save(allocation);
        portfolioRepository.save(portfolio);

        // Publish event
        FundsAllocatedEvent event = FundsAllocatedEvent.builder()
                .portfolioId(portfolioId.toString())
                .strategyId(strategyId)
                .strategyName(strategyName)
                .allocatedAmount(allocation.getAllocatedAmount())
                .allocationType(allocationType.name())
                .maxPositionSize(maxPositionSize)
                .build();
        eventPublisher.publish(event);

        log.info("Allocated ₹{} to strategy {}", allocation.getAllocatedAmount(), strategyId);

        return saved;
    }

    /**
     * Deallocate funds from a strategy
     */
    @Transactional
    public void deallocateFunds(Long portfolioId, String strategyId) {
        log.info("Deallocating funds from strategy {} in portfolio {}", strategyId, portfolioId);

        Portfolio portfolio = getPortfolio(portfolioId);
        StrategyAllocation allocation = strategyAllocationRepository
                .findByPortfolioIdAndStrategyId(portfolioId, strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy allocation not found"));

        // Check if there's any open exposure
        if (allocation.getCurrentExposure() > 0) {
            throw new IllegalStateException("Cannot deallocate funds while strategy has open positions");
        }

        // Remove allocation
        portfolio.deallocateFromStrategy(allocation);
        allocation.setIsActive(false);

        strategyAllocationRepository.save(allocation);
        portfolioRepository.save(portfolio);

        log.info("Deallocated ₹{} from strategy {}", allocation.getAllocatedAmount(), strategyId);
    }

    /**
     * Reserve capital for a new trade
     */
    @Transactional
    public void reserveCapital(Long portfolioId, String strategyId, Double amount,
                              String tradeId, Long instrumentToken, String tradingSymbol,
                              Integer quantity, Double averagePrice) {
        log.debug("Reserving ₹{} for trade {} in strategy {}", amount, tradeId, strategyId);

        Portfolio portfolio = getPortfolio(portfolioId);
        StrategyAllocation allocation = strategyAllocationRepository
                .findByPortfolioIdAndStrategyIdAndIsActiveTrue(portfolioId, strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy allocation not found: " + strategyId));

        // Check if position can be taken
        if (!allocation.canTakePosition(amount)) {
            throw new IllegalStateException(
                    String.format("Cannot take position. Amount: ₹%.2f, Max: ₹%.2f, Available: ₹%.2f",
                            amount, allocation.getMaxPositionSize(), allocation.getAvailableCapital()));
        }

        if (!portfolio.canTakePosition(amount)) {
            throw new IllegalStateException("Portfolio exposure limit would be breached");
        }

        // Reserve capital
        allocation.reserveCapital(amount);
        portfolio.reserveCapital(amount);

        strategyAllocationRepository.save(allocation);
        portfolioRepository.save(portfolio);

        // Publish event
        PositionOpenedEvent event = PositionOpenedEvent.builder()
                .portfolioId(portfolioId.toString())
                .strategyId(strategyId)
                .tradeId(tradeId)
                .instrumentToken(instrumentToken)
                .tradingSymbol(tradingSymbol)
                .quantity(quantity)
                .averagePrice(averagePrice)
                .totalValue(amount)
                .build();
        eventPublisher.publish(event);

        log.debug("Reserved ₹{} for trade {}", amount, tradeId);
    }

    /**
     * Release capital when a trade is closed
     */
    @Transactional
    public void releaseCapital(Long portfolioId, String strategyId, Double amount, Double pnl,
                              String tradeId, Long instrumentToken, String tradingSymbol,
                              Integer quantity, Double entryPrice, Double exitPrice) {
        log.debug("Releasing ₹{} from trade {} with P&L: ₹{}", amount, tradeId, pnl);

        Portfolio portfolio = getPortfolio(portfolioId);
        StrategyAllocation allocation = strategyAllocationRepository
                .findByPortfolioIdAndStrategyIdAndIsActiveTrue(portfolioId, strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy allocation not found: " + strategyId));

        // Release capital
        allocation.releaseCapital(amount, pnl);
        portfolio.releaseCapital(amount, pnl);

        strategyAllocationRepository.save(allocation);
        portfolioRepository.save(portfolio);

        // Publish event
        Double pnlPercentage = ((exitPrice - entryPrice) / entryPrice) * 100.0;
        PositionClosedEvent event = PositionClosedEvent.builder()
                .portfolioId(portfolioId.toString())
                .strategyId(strategyId)
                .tradeId(tradeId)
                .instrumentToken(instrumentToken)
                .tradingSymbol(tradingSymbol)
                .quantity(quantity)
                .averageEntryPrice(entryPrice)
                .averageExitPrice(exitPrice)
                .pnl(pnl)
                .pnlPercentage(pnlPercentage)
                .build();
        eventPublisher.publish(event);

        log.debug("Released ₹{} from trade {} with P&L: ₹{}", amount, tradeId, pnl);
    }

    /**
     * Update unrealized P&L for a portfolio
     */
    @Transactional
    public void updateUnrealizedPnl(Long portfolioId, Double unrealizedPnl) {
        Portfolio portfolio = getPortfolio(portfolioId);
        portfolio.updateUnrealizedPnl(unrealizedPnl);
        portfolioRepository.save(portfolio);
    }

    /**
     * Update unrealized P&L for a strategy
     */
    @Transactional
    public void updateStrategyUnrealizedPnl(Long portfolioId, String strategyId, Double unrealizedPnl) {
        StrategyAllocation allocation = strategyAllocationRepository
                .findByPortfolioIdAndStrategyIdAndIsActiveTrue(portfolioId, strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy allocation not found: " + strategyId));

        allocation.updateUnrealizedPnl(unrealizedPnl);
        strategyAllocationRepository.save(allocation);
    }

    /**
     * Get all active allocations for a portfolio
     */
    public List<StrategyAllocation> getPortfolioAllocations(Long portfolioId) {
        return strategyAllocationRepository.findByPortfolioIdAndIsActiveTrue(portfolioId);
    }

    /**
     * Get allocation for a specific strategy in a portfolio
     */
    public StrategyAllocation getStrategyAllocation(Long portfolioId, String strategyId) {
        return strategyAllocationRepository
                .findByPortfolioIdAndStrategyIdAndIsActiveTrue(portfolioId, strategyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Strategy allocation not found for strategy: " + strategyId));
    }

    /**
     * Check if portfolio is within risk limits
     */
    public boolean isWithinRiskLimits(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        return !portfolio.isMaxDrawdownBreached();
    }

    /**
     * Get portfolio summary
     */
    public PortfolioSummary getPortfolioSummary(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        List<StrategyAllocation> allocations = getPortfolioAllocations(portfolioId);

        return PortfolioSummary.builder()
                .portfolioId(portfolioId)
                .name(portfolio.getName())
                .totalCapital(portfolio.getTotalCapital())
                .availableCapital(portfolio.getAvailableCapital())
                .allocatedCapital(portfolio.getAllocatedCapital())
                .utilizedCapital(portfolio.getUtilizedCapital())
                .realizedPnl(portfolio.getRealizedPnl())
                .unrealizedPnl(portfolio.getUnrealizedPnl())
                .totalValue(portfolio.getTotalValue())
                .utilizationPercent(portfolio.getUtilizationPercent())
                .currentDrawdown(portfolio.getCurrentDrawdown())
                .maxDrawdownPercent(portfolio.getMaxDrawdownPercent())
                .numberOfStrategies(allocations.size())
                .strategyAllocations(allocations)
                .build();
    }

    /**
     * Portfolio summary DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PortfolioSummary {
        private Long portfolioId;
        private String name;
        private Double totalCapital;
        private Double availableCapital;
        private Double allocatedCapital;
        private Double utilizedCapital;
        private Double realizedPnl;
        private Double unrealizedPnl;
        private Double totalValue;
        private Double utilizationPercent;
        private Double currentDrawdown;
        private Double maxDrawdownPercent;
        private Integer numberOfStrategies;
        private List<StrategyAllocation> strategyAllocations;
    }
}
