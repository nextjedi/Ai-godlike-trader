package com.nextjedi.trading.tipbasedtrading.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextjedi.trading.tipbasedtrading.controller.PortfolioController;
import com.nextjedi.trading.tipbasedtrading.models.Portfolio;
import com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation;
import com.nextjedi.trading.tipbasedtrading.repository.PortfolioRepository;
import com.nextjedi.trading.tipbasedtrading.repository.StrategyAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Portfolio API with TestContainers
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@DisplayName("Portfolio Integration Tests")
class PortfolioIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("trading_db_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private StrategyAllocationRepository strategyAllocationRepository;

    @BeforeEach
    void setUp() {
        strategyAllocationRepository.deleteAll();
        portfolioRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should create portfolio via API")
    void shouldCreatePortfolioViaApi() throws Exception {
        // Given
        PortfolioController.CreatePortfolioRequest request = PortfolioController.CreatePortfolioRequest.builder()
                .name("Integration Test Portfolio")
                .userId("testuser")
                .totalCapital(100000.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Test Portfolio"))
                .andExpect(jsonPath("$.totalCapital").value(100000.0))
                .andExpect(jsonPath("$.availableCapital").value(100000.0));

        // Verify database
        assertThat(portfolioRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should get portfolio by ID via API")
    void shouldGetPortfolioByIdViaApi() throws Exception {
        // Given
        Portfolio portfolio = Portfolio.builder()
                .name("Test Portfolio")
                .userId("testuser")
                .totalCapital(100000.0)
                .availableCapital(100000.0)
                .allocatedCapital(0.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .isActive(true)
                .build();
        Portfolio saved = portfolioRepository.save(portfolio);

        // When/Then
        mockMvc.perform(get("/api/v1/portfolio/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Test Portfolio"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should return 404 when portfolio not found")
    void shouldReturn404WhenPortfolioNotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/portfolio/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should allocate funds to strategy via API")
    void shouldAllocateFundsToStrategyViaApi() throws Exception {
        // Given
        Portfolio portfolio = Portfolio.builder()
                .name("Test Portfolio")
                .userId("testuser")
                .totalCapital(100000.0)
                .availableCapital(100000.0)
                .allocatedCapital(0.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .isActive(true)
                .build();
        Portfolio saved = portfolioRepository.save(portfolio);

        PortfolioController.AllocateFundsRequest request = PortfolioController.AllocateFundsRequest.builder()
                .strategyId("strategy1")
                .strategyName("Test Strategy")
                .allocationType(StrategyAllocation.AllocationType.PERCENTAGE)
                .allocationValue(20.0)
                .maxPositionSize(5000.0)
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/portfolio/{id}/allocate", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategyId").value("strategy1"))
                .andExpect(jsonPath("$.allocatedAmount").value(20000.0));

        // Verify database
        Portfolio updated = portfolioRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getAvailableCapital()).isEqualTo(80000.0);
        assertThat(updated.getAllocatedCapital()).isEqualTo(20000.0);
        assertThat(strategyAllocationRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given
        PortfolioController.CreatePortfolioRequest request = PortfolioController.CreatePortfolioRequest.builder()
                .name("")  // Invalid: blank name
                .userId("testuser")
                .totalCapital(100000.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should deallocate funds from strategy via API")
    void shouldDeallocateFundsFromStrategyViaApi() throws Exception {
        // Given
        Portfolio portfolio = Portfolio.builder()
                .name("Test Portfolio")
                .userId("testuser")
                .totalCapital(100000.0)
                .availableCapital(80000.0)
                .allocatedCapital(20000.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .isActive(true)
                .build();
        Portfolio saved = portfolioRepository.save(portfolio);

        StrategyAllocation allocation = StrategyAllocation.builder()
                .portfolio(saved)
                .strategyId("strategy1")
                .strategyName("Test Strategy")
                .allocationType(StrategyAllocation.AllocationType.PERCENTAGE)
                .allocationValue(20.0)
                .allocatedAmount(20000.0)
                .maxPositionSize(5000.0)
                .build();
        strategyAllocationRepository.save(allocation);

        // When/Then
        mockMvc.perform(delete("/api/v1/portfolio/{portfolioId}/allocate/{strategyId}", 
                        saved.getId(), "strategy1"))
                .andExpect(status().isOk());

        // Verify database
        Portfolio updated = portfolioRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getAvailableCapital()).isEqualTo(100000.0);
        assertThat(updated.getAllocatedCapital()).isEqualTo(0.0);
        assertThat(strategyAllocationRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"TRADER"})
    @DisplayName("Should get user portfolios via API")
    void shouldGetUserPortfoliosViaApi() throws Exception {
        // Given
        Portfolio portfolio1 = Portfolio.builder()
                .name("Portfolio 1")
                .userId("testuser")
                .totalCapital(100000.0)
                .availableCapital(100000.0)
                .allocatedCapital(0.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .isActive(true)
                .build();
        
        Portfolio portfolio2 = Portfolio.builder()
                .name("Portfolio 2")
                .userId("testuser")
                .totalCapital(200000.0)
                .availableCapital(200000.0)
                .allocatedCapital(0.0)
                .maxDrawdownPercent(15.0)
                .maxExposurePercent(70.0)
                .isActive(false)
                .build();

        portfolioRepository.save(portfolio1);
        portfolioRepository.save(portfolio2);

        // When/Then
        mockMvc.perform(get("/api/v1/portfolio/user/{userId}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Portfolio 1"))
                .andExpect(jsonPath("$[1].name").value("Portfolio 2"));
    }
}
