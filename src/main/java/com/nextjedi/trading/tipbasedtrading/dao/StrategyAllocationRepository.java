package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyAllocationRepository extends JpaRepository<StrategyAllocation, Long> {

    List<StrategyAllocation> findByPortfolioId(Long portfolioId);

    List<StrategyAllocation> findByPortfolioIdAndIsActiveTrue(Long portfolioId);

    Optional<StrategyAllocation> findByPortfolioIdAndStrategyId(Long portfolioId, String strategyId);

    Optional<StrategyAllocation> findByPortfolioIdAndStrategyIdAndIsActiveTrue(Long portfolioId, String strategyId);

    List<StrategyAllocation> findByStrategyId(String strategyId);

    List<StrategyAllocation> findByIsActiveTrue();
}
