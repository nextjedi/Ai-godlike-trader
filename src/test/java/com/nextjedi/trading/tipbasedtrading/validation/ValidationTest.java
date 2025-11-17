package com.nextjedi.trading.tipbasedtrading.validation;

import com.nextjedi.trading.tipbasedtrading.broker.dto.OrderRequest;
import com.nextjedi.trading.tipbasedtrading.controller.PortfolioController;
import com.nextjedi.trading.tipbasedtrading.models.InstrumentQuery;
import com.nextjedi.trading.tipbasedtrading.models.TipModelRequest;
import com.nextjedi.trading.tipbasedtrading.models.TradeType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Validation tests for DTOs
 */
@DisplayName("DTO Validation Tests")
class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // TipModelRequest Tests

    @Test
    @DisplayName("Should validate valid TipModelRequest")
    void shouldValidateValidTipModelRequest() {
        // Given
        InstrumentQuery instrument = new InstrumentQuery();
        instrument.setStrike(45000);
        instrument.setName("NIFTY");
        instrument.setInstrumentType("CE");
        instrument.setExpiry(Date.valueOf(LocalDate.now().plusDays(7)));

        TipModelRequest request = new TipModelRequest();
        request.setInstrument(instrument);
        request.setPrice(100);
        request.setStopLoss(90);
        request.setTarget(120);
        request.setType(TradeType.BUY);

        // When
        Set<ConstraintViolation<TipModelRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject TipModelRequest with null instrument")
    void shouldRejectTipModelRequestWithNullInstrument() {
        // Given
        TipModelRequest request = new TipModelRequest();
        request.setPrice(100);
        request.setStopLoss(90);
        request.setTarget(120);
        request.setType(TradeType.BUY);

        // When
        Set<ConstraintViolation<TipModelRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Instrument is required"));
    }

    @Test
    @DisplayName("Should reject TipModelRequest with negative price")
    void shouldRejectTipModelRequestWithNegativePrice() {
        // Given
        InstrumentQuery instrument = new InstrumentQuery();
        instrument.setStrike(45000);
        instrument.setName("NIFTY");
        instrument.setInstrumentType("CE");
        instrument.setExpiry(Date.valueOf(LocalDate.now().plusDays(7)));

        TipModelRequest request = new TipModelRequest();
        request.setInstrument(instrument);
        request.setPrice(-100);
        request.setStopLoss(90);
        request.setTarget(120);
        request.setType(TradeType.BUY);

        // When
        Set<ConstraintViolation<TipModelRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Price must be positive"));
    }

    // InstrumentQuery Tests

    @Test
    @DisplayName("Should validate valid InstrumentQuery")
    void shouldValidateValidInstrumentQuery() {
        // Given
        InstrumentQuery instrument = new InstrumentQuery();
        instrument.setStrike(45000);
        instrument.setName("NIFTY");
        instrument.setInstrumentType("CE");
        instrument.setExpiry(Date.valueOf(LocalDate.now().plusDays(7)));

        // When
        Set<ConstraintViolation<InstrumentQuery>> violations = validator.validate(instrument);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject InstrumentQuery with blank name")
    void shouldRejectInstrumentQueryWithBlankName() {
        // Given
        InstrumentQuery instrument = new InstrumentQuery();
        instrument.setStrike(45000);
        instrument.setName("");
        instrument.setInstrumentType("CE");
        instrument.setExpiry(Date.valueOf(LocalDate.now().plusDays(7)));

        // When
        Set<ConstraintViolation<InstrumentQuery>> violations = validator.validate(instrument);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Instrument name is required"));
    }

    // OrderRequest Tests

    @Test
    @DisplayName("Should validate valid OrderRequest")
    void shouldValidateValidOrderRequest() {
        // Given
        OrderRequest request = OrderRequest.builder()
                .exchange("NSE")
                .tradingSymbol("NIFTY25JAN45000CE")
                .transactionType(OrderRequest.TransactionType.BUY)
                .orderType(OrderRequest.OrderType.LIMIT)
                .quantity(50)
                .price(100.0)
                .product(OrderRequest.Product.MIS)
                .validity(OrderRequest.Validity.DAY)
                .build();

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject OrderRequest with blank exchange")
    void shouldRejectOrderRequestWithBlankExchange() {
        // Given
        OrderRequest request = OrderRequest.builder()
                .exchange("")
                .tradingSymbol("NIFTY25JAN45000CE")
                .transactionType(OrderRequest.TransactionType.BUY)
                .orderType(OrderRequest.OrderType.LIMIT)
                .quantity(50)
                .price(100.0)
                .product(OrderRequest.Product.MIS)
                .validity(OrderRequest.Validity.DAY)
                .build();

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Exchange is required"));
    }

    @Test
    @DisplayName("Should reject OrderRequest with negative quantity")
    void shouldRejectOrderRequestWithNegativeQuantity() {
        // Given
        OrderRequest request = OrderRequest.builder()
                .exchange("NSE")
                .tradingSymbol("NIFTY25JAN45000CE")
                .transactionType(OrderRequest.TransactionType.BUY)
                .orderType(OrderRequest.OrderType.LIMIT)
                .quantity(-50)
                .price(100.0)
                .product(OrderRequest.Product.MIS)
                .validity(OrderRequest.Validity.DAY)
                .build();

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Quantity must be positive"));
    }

    // PortfolioController.CreatePortfolioRequest Tests

    @Test
    @DisplayName("Should validate valid CreatePortfolioRequest")
    void shouldValidateValidCreatePortfolioRequest() {
        // Given
        PortfolioController.CreatePortfolioRequest request = PortfolioController.CreatePortfolioRequest.builder()
                .name("Test Portfolio")
                .userId("user123")
                .totalCapital(100000.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .build();

        // When
        Set<ConstraintViolation<PortfolioController.CreatePortfolioRequest>> violations = 
                validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject CreatePortfolioRequest with blank name")
    void shouldRejectCreatePortfolioRequestWithBlankName() {
        // Given
        PortfolioController.CreatePortfolioRequest request = PortfolioController.CreatePortfolioRequest.builder()
                .name("")
                .userId("user123")
                .totalCapital(100000.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .build();

        // When
        Set<ConstraintViolation<PortfolioController.CreatePortfolioRequest>> violations = 
                validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Should reject CreatePortfolioRequest with negative total capital")
    void shouldRejectCreatePortfolioRequestWithNegativeTotalCapital() {
        // Given
        PortfolioController.CreatePortfolioRequest request = PortfolioController.CreatePortfolioRequest.builder()
                .name("Test Portfolio")
                .userId("user123")
                .totalCapital(-100000.0)
                .maxDrawdownPercent(10.0)
                .maxExposurePercent(80.0)
                .build();

        // When
        Set<ConstraintViolation<PortfolioController.CreatePortfolioRequest>> violations = 
                validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("totalCapital"));
    }

    // PortfolioController.AllocateFundsRequest Tests

    @Test
    @DisplayName("Should validate valid AllocateFundsRequest")
    void shouldValidateValidAllocateFundsRequest() {
        // Given
        PortfolioController.AllocateFundsRequest request = PortfolioController.AllocateFundsRequest.builder()
                .strategyId("strategy1")
                .strategyName("Test Strategy")
                .allocationType(com.nextjedi.trading.tipbasedtrading.models.StrategyAllocation.AllocationType.PERCENTAGE)
                .allocationValue(20.0)
                .maxPositionSize(5000.0)
                .build();

        // When
        Set<ConstraintViolation<PortfolioController.AllocateFundsRequest>> violations = 
                validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject AllocateFundsRequest with null allocation type")
    void shouldRejectAllocateFundsRequestWithNullAllocationType() {
        // Given
        PortfolioController.AllocateFundsRequest request = PortfolioController.AllocateFundsRequest.builder()
                .strategyId("strategy1")
                .strategyName("Test Strategy")
                .allocationType(null)
                .allocationValue(20.0)
                .maxPositionSize(5000.0)
                .build();

        // When
        Set<ConstraintViolation<PortfolioController.AllocateFundsRequest>> violations = 
                validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("allocationType"));
    }
}
