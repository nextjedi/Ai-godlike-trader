# Event-Driven Trading System Architecture

## Overview

This document describes the refactored architecture of the AI Godlike Trader system, implementing event-driven design patterns, multi-broker support, portfolio management, and real-time reporting.

## Architecture Principles

1. **Event-Driven Architecture (EDA)**: All state changes emit domain events
2. **Strategy Pattern**: Abstract broker implementations for multi-platform support
3. **Domain-Driven Design (DDD)**: Rich domain models with business logic
4. **CQRS**: Separate read and write models where appropriate
5. **Microservices-Ready**: Loosely coupled components communicating via events

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Layer                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Trade API    │  │ Portfolio API│  │ Report API   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Application Layer                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Event Bus (Spring Events)                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│         │              │              │              │           │
│         ▼              ▼              ▼              ▼           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Trade    │  │Portfolio │  │ Tick     │  │ Report   │       │
│  │ Handler  │  │ Handler  │  │ Handler  │  │ Handler  │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Domain Layer                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Trade        │  │ Portfolio    │  │ Strategy     │          │
│  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                  Message Queue (Redis/Kafka)             │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   │
│  │  │ Tick Stream  │  │ Event Stream │  │ Order Stream │   │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Broker Abstraction Layer                    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   │
│  │  │ Zerodha      │  │ Future:      │  │ Future:      │   │   │
│  │  │ Broker       │  │ Upstox       │  │ Interactive  │   │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Reporting Infrastructure                    │   │
│  │  ┌──────────────┐  ┌──────────────┐                      │   │
│  │  │ PDF Generator│  │ Email Service│                      │   │
│  │  └──────────────┘  └──────────────┘                      │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Event Bus System

**Purpose**: Decouple components via publish-subscribe pattern

**Implementation**:
- Spring ApplicationEventPublisher for synchronous events
- Redis Pub/Sub for distributed events
- Kafka for high-throughput tick data streaming

**Event Categories**:
- **Trade Events**: TradeCreatedEvent, TradeEnteredEvent, TradeExitedEvent, TradeCompletedEvent
- **Order Events**: OrderPlacedEvent, OrderExecutedEvent, OrderCancelledEvent, OrderModifiedEvent
- **Tick Events**: TickReceivedEvent, MarketDataUpdatedEvent
- **Portfolio Events**: FundsAllocatedEvent, PositionOpenedEvent, PositionClosedEvent, MarginUpdatedEvent
- **Report Events**: ReportGeneratedEvent, ReportEmailedEvent

### 2. Broker Abstraction Layer

**Purpose**: Support multiple trading platforms using Strategy Pattern

**Interface**: `TradingBroker`
```java
public interface TradingBroker {
    String getName();
    OrderResponse placeOrder(OrderRequest request);
    OrderResponse modifyOrder(String orderId, OrderRequest request);
    OrderResponse cancelOrder(String orderId);
    List<Position> getPositions();
    Balance getBalance();
    List<Instrument> getInstruments(String exchange);
    void subscribeToTicks(List<Long> instrumentTokens, TickListener listener);
    void unsubscribeFromTicks(List<Long> instrumentTokens);
}
```

**Implementations**:
- `ZerodhaBroker`: Current implementation
- `UpstoxBroker`: Future
- `InteractiveBrokersBroker`: Future
- `MockBroker`: For testing

### 3. Portfolio Management System

**Purpose**: Manage capital allocation across multiple strategies

**Components**:
- **Portfolio Aggregate**: Root entity managing overall portfolio
- **Strategy Allocation**: Defines fund allocation per strategy
- **Position Manager**: Tracks open positions and exposure
- **Risk Manager**: Enforces position limits and risk rules

**Features**:
- Capital allocation by percentage or fixed amount
- Per-strategy max exposure limits
- Overall portfolio risk limits
- Real-time P&L tracking
- Margin utilization monitoring

**Example Configuration**:
```java
Portfolio portfolio = Portfolio.builder()
    .totalCapital(1000000.0) // 10 lakhs
    .strategyAllocations(List.of(
        StrategyAllocation.builder()
            .strategyId("tip-based-trading")
            .allocationType(AllocationType.PERCENTAGE)
            .allocation(40.0) // 40% = 4 lakhs
            .maxPositionSize(15000.0)
            .build(),
        StrategyAllocation.builder()
            .strategyId("momentum-strategy")
            .allocationType(AllocationType.PERCENTAGE)
            .allocation(30.0) // 30% = 3 lakhs
            .maxPositionSize(20000.0)
            .build()
    ))
    .build();
```

### 4. Strategy Management System

**Purpose**: Support multiple trading strategies with different logic

**Components**:
- **TradingStrategy Interface**: Common contract for all strategies
- **Strategy Registry**: Manages available strategies
- **Strategy Executor**: Executes strategy logic
- **Signal Generator**: Generates trading signals

**Strategy Interface**:
```java
public interface TradingStrategy {
    String getStrategyId();
    String getStrategyName();
    boolean canExecute(Market market, Portfolio portfolio);
    Optional<TradeSignal> generateSignal(MarketData data);
    void onTickReceived(Tick tick);
    void onTradeEntered(Trade trade);
    void onTradeExited(Trade trade);
}
```

**Built-in Strategies**:
- **TipBasedStrategy**: Current implementation (external signals)
- **MomentumStrategy**: Future
- **MeanReversionStrategy**: Future
- **ArbitrageStrategy**: Future

### 5. Tick Data Streaming

**Purpose**: Real-time market data ingestion and distribution

**Architecture**:
```
Broker WebSocket → Tick Producer → Redis Stream/Kafka Topic → Tick Consumer → Event Bus → Strategies
```

**Components**:
- **Tick Producer**: Receives ticks from broker, publishes to queue
- **Tick Consumer**: Subscribes to queue, distributes to strategies
- **Tick Repository**: Stores tick data for analysis
- **Market Data Service**: Provides latest quotes and historical data

**Data Flow**:
1. WebSocket receives tick from broker
2. Tick normalized to common format
3. Published to Redis Stream (fast) or Kafka (persistent)
4. Multiple consumers process ticks independently
5. Strategies receive relevant ticks via event bus

**Storage Strategy**:
- **Hot Data** (last 1 day): Redis with TTL
- **Warm Data** (last 30 days): Time-series database (InfluxDB/TimescaleDB)
- **Cold Data** (historical): S3/object storage

### 6. Reporting System

**Purpose**: Generate and distribute trading reports

**Components**:
- **Report Generator**: Creates reports from trade data
- **PDF Service**: Generates PDF documents
- **Email Service**: Sends reports via email
- **Report Scheduler**: Automates report generation

**Report Types**:
- **Daily P&L Report**: Daily profit/loss summary
- **Trade Journal**: Detailed trade log
- **Portfolio Summary**: Current positions and allocations
- **Performance Analytics**: Returns, Sharpe ratio, drawdown
- **Risk Report**: Exposure, margin utilization

**Report Formats**:
- PDF (Apache PDFBox or iText)
- Excel (Apache POI)
- HTML email
- JSON API response

**Scheduling**:
- End of day: Daily P&L report
- Weekly: Performance summary
- Monthly: Comprehensive analytics
- On-demand: Via API

## Technology Stack

### Core Framework
- **Spring Boot 3.0+**: Application framework
- **Java 17+**: Programming language
- **Maven**: Build tool

### Data Storage
- **PostgreSQL**: Primary database (replacing H2)
- **Redis**: Caching, session management, real-time data
- **InfluxDB/TimescaleDB**: Time-series tick data (optional)

### Message Queue
- **Redis Streams**: Lightweight, fast, good for tick data
- **Apache Kafka**: High-throughput, persistent, fault-tolerant (alternative)

### Reporting
- **Apache PDFBox** or **iText**: PDF generation
- **Apache POI**: Excel generation
- **Spring Mail**: Email delivery
- **Thymeleaf**: HTML template engine

### Monitoring & Observability
- **Spring Actuator**: Health checks, metrics
- **Micrometer**: Metrics collection
- **Prometheus**: Metrics storage (optional)
- **Grafana**: Dashboards (optional)
- **SLF4J + Logback**: Logging

### Security
- **Spring Security**: Authentication & authorization
- **Vault**: Secret management
- **Environment Variables**: Configuration externalization

## Event Flow Examples

### Example 1: Trade Execution Flow

```
1. External Signal Arrives
   POST /api/v1/trades/tip
   ↓
2. TradeController validates request
   ↓
3. TradeApplicationService.createTrade()
   ↓
4. Portfolio checks available capital
   ↓
5. TradeAggregate created with status NEW
   ↓
6. TradeCreatedEvent published
   ↓
7. Multiple handlers react:
   - TickSubscriptionHandler: Subscribe to instrument ticks
   - PortfolioHandler: Reserve capital
   - AuditHandler: Log trade creation
   - NotificationHandler: Send notification (optional)
   ↓
8. Tick arrives from broker WebSocket
   ↓
9. TickProducer publishes to Redis Stream
   ↓
10. TickConsumer receives and publishes TickReceivedEvent
    ↓
11. TradeExecutionHandler evaluates entry conditions
    ↓
12. If conditions met: placeEntryOrder()
    ↓
13. Broker executes order
    ↓
14. Order callback arrives → OrderExecutedEvent
    ↓
15. TradeAggregate.markAsEntered()
    ↓
16. TradeEnteredEvent published
    ↓
17. StopLossHandler places exit order
    ↓
18. TrailingStopHandler starts monitoring
    ↓
19. Exit order executes → TradeCompletedEvent
    ↓
20. Handlers react:
    - PortfolioHandler: Update P&L, release capital
    - TickSubscriptionHandler: Unsubscribe
    - ReportHandler: Update statistics
```

### Example 2: Portfolio Allocation Flow

```
1. Configure portfolio allocations
   POST /api/v1/portfolio/allocations
   ↓
2. PortfolioApplicationService.allocateFunds()
   ↓
3. Validate total allocation ≤ 100%
   ↓
4. FundsAllocatedEvent published for each strategy
   ↓
5. Strategy handlers react:
   - Update available capital
   - Recalculate position limits
   - Adjust risk parameters
```

### Example 3: Report Generation Flow

```
1. Scheduled trigger: End of day (3:30 PM)
   ↓
2. ReportScheduler.generateDailyReport()
   ↓
3. Fetch trades from database
   ↓
4. Calculate P&L, metrics
   ↓
5. PDFReportGenerator.generate()
   ↓
6. ReportGeneratedEvent published
   ↓
7. EmailHandler sends report via SMTP
   ↓
8. ReportEmailedEvent published
   ↓
9. AuditHandler logs successful delivery
```

## Configuration Management

### Externalized Configuration

**application.yml**:
```yaml
trading:
  portfolio:
    total-capital: ${PORTFOLIO_CAPITAL:1000000}
    default-max-position-size: ${MAX_POSITION_SIZE:15000}

  brokers:
    zerodha:
      enabled: true
      user-id: ${ZERODHA_USER_ID}
      api-key: ${ZERODHA_API_KEY}
      api-secret: ${ZERODHA_API_SECRET}
    upstox:
      enabled: false

  tick-data:
    enabled: true
    provider: redis  # or kafka
    redis:
      stream-key: trading:ticks
      consumer-group: tick-processors
    kafka:
      topic: trading-ticks
      bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}

  reporting:
    enabled: true
    output-directory: ${REPORT_DIR:/var/reports}
    email:
      enabled: true
      from: ${EMAIL_FROM:trading@example.com}
      recipients: ${EMAIL_TO:admin@example.com}
    schedule:
      daily: "0 30 15 * * MON-FRI"  # 3:30 PM on weekdays
```

### Secret Management

**Using HashiCorp Vault** (recommended):
```java
@Configuration
@EnableVault
public class VaultConfig {
    @Bean
    public VaultTemplate vaultTemplate() {
        // Configure Vault connection
    }
}
```

**Using Spring Cloud Config** (alternative):
```yaml
spring:
  cloud:
    config:
      uri: http://config-server:8888
```

## Database Schema

### Core Tables

**portfolios**:
- id (PK)
- name
- total_capital
- available_capital
- allocated_capital
- created_at
- updated_at

**strategy_allocations**:
- id (PK)
- portfolio_id (FK)
- strategy_id
- allocation_type (PERCENTAGE, FIXED_AMOUNT)
- allocation_value
- max_position_size
- current_exposure
- is_active

**trades** (enhanced):
- id (PK)
- portfolio_id (FK)
- strategy_id
- broker_name
- instrument_token
- trigger_price
- entry_price
- exit_price
- quantity
- stop_loss
- target
- trade_status
- trade_type
- pnl
- created_at
- entered_at
- exited_at

**positions**:
- id (PK)
- portfolio_id (FK)
- strategy_id
- broker_name
- instrument_token
- quantity
- average_price
- current_price
- unrealized_pnl
- created_at
- updated_at

**tick_data** (time-series):
- instrument_token
- timestamp
- last_price
- volume
- bid_price
- ask_price
- open_interest

**reports**:
- id (PK)
- report_type
- report_date
- file_path
- file_size
- generated_at
- emailed_at
- status

## Migration Path

### Phase 1: Foundation (Week 1-2)
1. Set up Redis/Kafka infrastructure
2. Implement event bus and core domain events
3. Create broker abstraction layer
4. Migrate Zerodha to new abstraction
5. Externalize configuration and secrets

### Phase 2: Portfolio & Strategy (Week 3-4)
6. Implement portfolio management system
7. Create strategy abstraction
8. Migrate existing tip-based logic to new strategy
9. Implement fund allocation logic

### Phase 3: Tick Data & Reporting (Week 5-6)
10. Implement tick data streaming with Redis/Kafka
11. Create reporting service
12. Implement PDF generation
13. Set up email notifications
14. Create report scheduler

### Phase 4: Testing & Documentation (Week 7-8)
15. Comprehensive unit tests
16. Integration tests
17. Performance testing
18. Documentation updates
19. Deployment guides

## Performance Considerations

1. **Event Processing**: Use async handlers to avoid blocking
2. **Tick Data**: Use Redis Streams for sub-millisecond latency
3. **Database**: Connection pooling, read replicas for queries
4. **Caching**: Cache instruments, market data, frequently accessed data
5. **Rate Limiting**: Protect broker APIs from excessive calls

## Security Best Practices

1. **Secrets**: Never commit secrets to code; use Vault or env vars
2. **API Security**: JWT authentication for APIs
3. **Encryption**: Encrypt sensitive data at rest
4. **Audit Logging**: Log all trade operations
5. **Rate Limiting**: Protect public APIs

## Monitoring & Alerting

1. **Metrics to Track**:
   - Trade execution latency
   - Order success/failure rates
   - P&L by strategy
   - System health (CPU, memory, connections)
   - Event processing rates

2. **Alerts**:
   - Failed orders
   - Position limit breaches
   - System errors
   - Unusual P&L movements

## Conclusion

This architecture provides:
- **Scalability**: Event-driven design supports horizontal scaling
- **Extensibility**: Easy to add new brokers and strategies
- **Maintainability**: Clean separation of concerns
- **Testability**: Mockable interfaces and isolated components
- **Observability**: Comprehensive logging and monitoring
- **Reliability**: Fault-tolerant with queue-based processing
