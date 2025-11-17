package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.exception.InsufficientFundsException;
import com.nextjedi.trading.tipbasedtrading.exception.ResourceNotFoundException;
import com.nextjedi.trading.tipbasedtrading.models.Portfolio;
import com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation;
import com.nextjedi.trading.tipbasedtrading.repository.PortfolioRepository;
import com.nextjedi.trading.tipbasedtrading.repository.StrategyAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortfolioService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Portfolio Service Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StrategyAllocationRepository strategyAllocationRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testPortfolio = Portfolio.builder()
                .id(1L)
                .name("Test Portfolio")
                .userId("testuser")
                .totalCapital(100000.0)
                .availableCapital(100000.0)
                .allocatedCapital(0.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .strategyAllocations(new ArrayList<>())
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create portfolio successfully")
    void shouldCreatePortfolioSuccessfully() {
        // Given
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        // When
        Portfolio created = portfolioService.createPortfolio(testPortfolio);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Portfolio");
        assertThat(created.getTotalCapital()).isEqualTo(100000.0);
        assertThat(created.getAvailableCapital()).isEqualTo(100000.0);
        verify(portfolioRepository).save(testPortfolio);
    }

    @Test
    @DisplayName("Should get portfolio by ID")
    void shouldGetPortfolioById() {
        // Given
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

        // When
        Portfolio found = portfolioService.getPortfolio(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("Test Portfolio");
    }

    @Test
    @DisplayName("Should throw exception when portfolio not found")
    void shouldThrowExceptionWhenPortfolioNotFound() {
        // Given
        when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> portfolioService.getPortfolio(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Portfolio not found");
    }

    @Test
    @DisplayName("Should get user portfolios")
    void shouldGetUserPortfolios() {
        // Given
        Portfolio portfolio2 = Portfolio.builder()
                .id(2L)
                .name("Test Portfolio 2")
                .userId("testuser")
                .build();
        
        when(portfolioRepository.findByUserId("testuser"))
                .thenReturn(Arrays.asList(testPortfolio, portfolio2));

        // When
        List<Portfolio> portfolios = portfolioService.getUserPortfolios("testuser");

        // Then
        assertThat(portfolios).hasSize(2);
        assertThat(portfolios).extracting("name")
                .contains("Test Portfolio", "Test Portfolio 2");
    }

    @Test
    @DisplayName("Should get user active portfolio")
    void shouldGetUserActivePortfolio() {
        // Given
        when(portfolioRepository.findByUserIdAndIsActive("testuser", true))
                .thenReturn(Optional.of(testPortfolio));

        // When
        Portfolio active = portfolioService.getUserActivePortfolio("testuser");

        // Then
        assertThat(active).isNotNull();
        assertThat(active.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should allocate funds to strategy successfully")
    void shouldAllocateFundsToStrategySuccessfully() {
        // Given
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(strategyAllocationRepository.save(any(StrategyAllocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        // When
        StrategyAllocation allocation = portfolioService.allocateFunds(
                1L,
                "strategy1",
                "Test Strategy",
                StrategyAllocation.AllocationType.PERCENTAGE,
                20.0,  // 20% of total capital
                5000.0
        );

        // Then
        assertThat(allocation).isNotNull();
        assertThat(allocation.getStrategyId()).isEqualTo("strategy1");
        assertThat(allocation.getAllocatedAmount()).isEqualTo(20000.0); // 20% of 100k
        
        // Verify portfolio was updated
        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository).save(portfolioCaptor.capture());
        Portfolio updatedPortfolio = portfolioCaptor.getValue();
        assertThat(updatedPortfolio.getAvailableCapital()).isEqualTo(80000.0);
        assertThat(updatedPortfolio.getAllocatedCapital()).isEqualTo(20000.0);
    }

    @Test
    @DisplayName("Should allocate fixed amount to strategy")
    void shouldAllocateFixedAmountToStrategy() {
        // Given
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(strategyAllocationRepository.save(any(StrategyAllocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        // When
        StrategyAllocation allocation = portfolioService.allocateFunds(
                1L,
                "strategy2",
                "Test Strategy 2",
                StrategyAllocation.AllocationType.FIXED,
                15000.0,  // Fixed amount
                3000.0
        );

        // Then
        assertThat(allocation).isNotNull();
        assertThat(allocation.getAllocatedAmount()).isEqualTo(15000.0);
        assertThat(allocation.getAllocationType()).isEqualTo(StrategyAllocation.AllocationType.FIXED);
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds for allocation")
    void shouldThrowExceptionWhenInsufficientFundsForAllocation() {
        // Given
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

        // When/Then
        assertThatThrownBy(() -> portfolioService.allocateFunds(
                1L,
                "strategy3",
                "Test Strategy 3",
                StrategyAllocation.AllocationType.FIXED,
                150000.0,  // More than available
                5000.0
        ))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient available capital");
    }

    @Test
    @DisplayName("Should deallocate funds from strategy")
    void shouldDeallocateFundsFromStrategy() {
        // Given
        StrategyAllocation allocation = StrategyAllocation.builder()
                .id(1L)
                .portfolio(testPortfolio)
                .strategyId("strategy1")
                .allocatedAmount(20000.0)
                .build();
        
        testPortfolio.setAvailableCapital(80000.0);
        testPortfolio.setAllocatedCapital(20000.0);
        testPortfolio.getStrategyAllocations().add(allocation);

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(strategyAllocationRepository.findByPortfolioAndStrategyId(testPortfolio, "strategy1"))
                .thenReturn(Optional.of(allocation));

        // When
        portfolioService.deallocateFunds(1L, "strategy1");

        // Then
        verify(strategyAllocationRepository).delete(allocation);
        
        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository).save(portfolioCaptor.capture());
        Portfolio updatedPortfolio = portfolioCaptor.getValue();
        assertThat(updatedPortfolio.getAvailableCapital()).isEqualTo(100000.0);
        assertThat(updatedPortfolio.getAllocatedCapital()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should get portfolio allocations")
    void shouldGetPortfolioAllocations() {
        // Given
        StrategyAllocation allocation1 = StrategyAllocation.builder()
                .strategyId("strategy1")
                .allocatedAmount(20000.0)
                .build();
        StrategyAllocation allocation2 = StrategyAllocation.builder()
                .strategyId("strategy2")
                .allocatedAmount(30000.0)
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(strategyAllocationRepository.findByPortfolio(testPortfolio))
                .thenReturn(Arrays.asList(allocation1, allocation2));

        // When
        List<StrategyAllocation> allocations = portfolioService.getPortfolioAllocations(1L);

        // Then
        assertThat(allocations).hasSize(2);
        assertThat(allocations).extracting("strategyId")
                .contains("strategy1", "strategy2");
    }

    @Test
    @DisplayName("Should get strategy allocation")
    void shouldGetStrategyAllocation() {
        // Given
        StrategyAllocation allocation = StrategyAllocation.builder()
                .strategyId("strategy1")
                .allocatedAmount(20000.0)
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(strategyAllocationRepository.findByPortfolioAndStrategyId(testPortfolio, "strategy1"))
                .thenReturn(Optional.of(allocation));

        // When
        StrategyAllocation found = portfolioService.getStrategyAllocation(1L, "strategy1");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getStrategyId()).isEqualTo("strategy1");
    }

    @Test
    @DisplayName("Should throw exception when strategy allocation not found")
    void shouldThrowExceptionWhenStrategyAllocationNotFound() {
        // Given
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(strategyAllocationRepository.findByPortfolioAndStrategyId(testPortfolio, "strategy999"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> portfolioService.getStrategyAllocation(1L, "strategy999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Strategy allocation not found");
    }
}
