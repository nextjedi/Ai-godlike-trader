package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.Portfolio;
import com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation;
import com.nextjedi.trading.tipbasedtrading.service.PortfolioService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * REST API for portfolio management
 */
@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * Create a new portfolio
     */
    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        log.info("Creating portfolio: {}", request.getName());

        Portfolio portfolio = Portfolio.builder()
                .name(request.getName())
                .userId(request.getUserId())
                .totalCapital(request.getTotalCapital())
                .maxDrawdownPercent(request.getMaxDrawdownPercent())
                .maxExposurePercent(request.getMaxExposurePercent())
                .build();

        Portfolio created = portfolioService.createPortfolio(portfolio);
        return ResponseEntity.ok(created);
    }

    /**
     * Get portfolio by ID
     */
    @GetMapping("/{portfolioId}")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable Long portfolioId) {
        Portfolio portfolio = portfolioService.getPortfolio(portfolioId);
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Get portfolio summary
     */
    @GetMapping("/{portfolioId}/summary")
    public ResponseEntity<PortfolioService.PortfolioSummary> getPortfolioSummary(@PathVariable Long portfolioId) {
        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary(portfolioId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get all portfolios for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Portfolio>> getUserPortfolios(@PathVariable String userId) {
        List<Portfolio> portfolios = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(portfolios);
    }

    /**
     * Get user's active portfolio
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Portfolio> getUserActivePortfolio(@PathVariable String userId) {
        Portfolio portfolio = portfolioService.getUserActivePortfolio(userId);
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Allocate funds to a strategy
     */
    @PostMapping("/{portfolioId}/allocate")
    public ResponseEntity<StrategyAllocation> allocateFunds(
            @PathVariable Long portfolioId,
            @Valid @RequestBody AllocateFundsRequest request) {

        log.info("Allocating funds to strategy {} in portfolio {}", request.getStrategyId(), portfolioId);

        StrategyAllocation allocation = portfolioService.allocateFunds(
                portfolioId,
                request.getStrategyId(),
                request.getStrategyName(),
                request.getAllocationType(),
                request.getAllocationValue(),
                request.getMaxPositionSize()
        );

        return ResponseEntity.ok(allocation);
    }

    /**
     * Deallocate funds from a strategy
     */
    @DeleteMapping("/{portfolioId}/allocate/{strategyId}")
    public ResponseEntity<Void> deallocateFunds(
            @PathVariable Long portfolioId,
            @PathVariable String strategyId) {

        log.info("Deallocating funds from strategy {} in portfolio {}", strategyId, portfolioId);

        portfolioService.deallocateFunds(portfolioId, strategyId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all strategy allocations for a portfolio
     */
    @GetMapping("/{portfolioId}/allocations")
    public ResponseEntity<List<StrategyAllocation>> getPortfolioAllocations(@PathVariable Long portfolioId) {
        List<StrategyAllocation> allocations = portfolioService.getPortfolioAllocations(portfolioId);
        return ResponseEntity.ok(allocations);
    }

    /**
     * Get strategy allocation
     */
    @GetMapping("/{portfolioId}/allocations/{strategyId}")
    public ResponseEntity<StrategyAllocation> getStrategyAllocation(
            @PathVariable Long portfolioId,
            @PathVariable String strategyId) {

        StrategyAllocation allocation = portfolioService.getStrategyAllocation(portfolioId, strategyId);
        return ResponseEntity.ok(allocation);
    }

    // DTOs

    @Data
    @Builder
    public static class CreatePortfolioRequest {
        @NotBlank
        private String name;

        @NotBlank
        private String userId;

        @NotNull
        @Positive
        private Double totalCapital;

        @NotNull
        @Positive
        private Double maxDrawdownPercent;

        @NotNull
        @Positive
        private Double maxExposurePercent;
    }

    @Data
    @Builder
    public static class AllocateFundsRequest {
        @NotBlank
        private String strategyId;

        @NotBlank
        private String strategyName;

        @NotNull
        private StrategyAllocation.AllocationType allocationType;

        @NotNull
        @Positive
        private Double allocationValue;

        @NotNull
        @Positive
        private Double maxPositionSize;
    }
}
